package io.payguard.userservice.integration.event.outbox;

import io.micrometer.core.instrument.MeterRegistry;
import io.payguard.userservice.application.event.ClaimedOutboxEvent;
import io.payguard.userservice.application.event.OutboxEventPublicationRepository;
import io.payguard.userservice.application.id.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "outbox.publisher",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxEventPublisher {

    private static final String EVENT_ID_HEADER = "eventId";
    private static final String EVENT_TYPE_HEADER = "eventType";
    private static final String CORRELATION_ID_HEADER = "correlationId";

    private final OutboxEventPublicationRepository publicationRepository;
    private final OutboxPublisherProperties properties;
    private final OutboxPublicationBackoff backoff;
    private final OutboxPublicationErrorSanitizer errorSanitizer;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IdGenerator idGenerator;
    private final MeterRegistry meterRegistry;

    @Scheduled(
            fixedDelayString =
                    "${outbox.publisher.poll-interval:1s}"
    )
    public void publishPendingEvents() {

        UUID claimId = idGenerator.generate();

        List<ClaimedOutboxEvent> events =
                publicationRepository.claimBatch(
                        claimId,
                        properties.leaseDuration(),
                        properties.batchSize()
                );

        events.forEach(this::publish);
    }

    private void publish(ClaimedOutboxEvent event) {

        try {

            kafkaTemplate
                    .send(toProducerRecord(event))
                    .get(
                            properties.sendTimeout().toMillis(),
                            TimeUnit.MILLISECONDS
                    );

            markPublished(event);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();
            handlePublicationFailure(
                    event,
                    exception
            );

        } catch (Exception exception) {

            handlePublicationFailure(
                    event,
                    exception
            );
        }
    }

    private ProducerRecord<String, String> toProducerRecord(ClaimedOutboxEvent event) {

        ProducerRecord<String, String> record = new ProducerRecord<>(properties.topic(), event.aggregateId(), event.eventBody());

        addHeader(
                record,
                EVENT_ID_HEADER,
                event.id().toString()
        );

        addHeader(
                record,
                EVENT_TYPE_HEADER,
                event.eventType()
        );

        if (event.correlationId() != null && !event.correlationId().isBlank()) {

            addHeader(
                    record,
                    CORRELATION_ID_HEADER,
                    event.correlationId()
            );
        }

        return record;
    }

    private void addHeader(
            ProducerRecord<String, String> record,
            String name,
            String value
    ) {

        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }

    private void markPublished(ClaimedOutboxEvent event) {

        boolean updated =
                publicationRepository.markPublished(
                        event.id(),
                        event.claimId()
                );

        if (!updated) {

            meterRegistry
                    .counter(
                            "outbox.publication.lost_claim"
                    )
                    .increment();

            log.warn(
                    "Kafka acknowledged outbox event [{}], but its claim [{}] is no longer owned by this publisher.",
                    event.id(),
                    event.claimId()
            );

            return;
        }

        meterRegistry
                .counter(
                        "outbox.publication.published"
                )
                .increment();

        log.info(
                "Published outbox event [{}] of type [{}] for aggregate [{}].",
                event.id(),
                event.eventType(),
                event.aggregateId()
        );
    }

    private void handlePublicationFailure(ClaimedOutboxEvent event, Exception exception) {

        int failedAttempt = event.attempts() + 1;
        String sanitizedError = errorSanitizer.sanitize(exception);

        if (failedAttempt >= properties.maxAttempts()) {

            markFailed(
                    event,
                    sanitizedError,
                    exception
            );

            return;
        }

        reschedule(
                event,
                failedAttempt,
                sanitizedError,
                exception
        );
    }

    private void reschedule(
            ClaimedOutboxEvent event,
            int failedAttempt,
            String sanitizedError,
            Exception exception
    ) {

        Duration retryBackoff =
                backoff.forAttempt(failedAttempt);

        boolean updated =
                publicationRepository.reschedule(
                        event.id(),
                        event.claimId(),
                        retryBackoff,
                        sanitizedError
                );

        if (!updated) {
            logLostClaimOnFailure(
                    event,
                    exception
            );
            return;
        }

        meterRegistry
                .counter(
                        "outbox.publication.retry"
                )
                .increment();

        log.warn(
                "Failed to publish outbox event [{}] on attempt [{}]. Retrying after [{}].",
                event.id(),
                failedAttempt,
                retryBackoff,
                exception
        );
    }

    private void markFailed(
            ClaimedOutboxEvent event,
            String sanitizedError,
            Exception exception
    ) {

        boolean updated =
                publicationRepository.markFailed(
                        event.id(),
                        event.claimId(),
                        sanitizedError
                );

        if (!updated) {
            logLostClaimOnFailure(
                    event,
                    exception
            );
            return;
        }

        meterRegistry
                .counter(
                        "outbox.publication.failed"
                )
                .increment();

        log.error(
                "Outbox event [{}] exhausted [{}] publication attempts and requires operational recovery.",
                event.id(),
                properties.maxAttempts(),
                exception
        );
    }

    private void logLostClaimOnFailure(
            ClaimedOutboxEvent event,
            Exception exception
    ) {

        meterRegistry
                .counter(
                        "outbox.publication.lost_claim"
                )
                .increment();

        log.warn(
                "Unable to record publication failure for outbox event [{}] because claim [{}] is no longer owned by this publisher.",
                event.id(),
                event.claimId(),
                exception
        );
    }
}
