package io.payguard.userservice.integration.event.outbox;

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
    private final OutboxPublicationMetrics metrics;

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

        events.forEach(event -> {
            recordRecoveredExpiredClaim(event);
            publish(event);
        });
    }

    private void recordRecoveredExpiredClaim(
            ClaimedOutboxEvent event
    ) {

        if (!event.reclaimedExpiredClaim()) {
            return;
        }

        metrics.recordStaleClaimRecovered();

        log.warn(
                "Reclaimed expired outbox event [{}] claim [{}] with new claim [{}] for aggregate type [{}], aggregate [{}] and correlation [{}].",
                event.id(),
                event.previousClaimId(),
                event.claimId(),
                event.aggregateType(),
                event.aggregateId(),
                event.correlationId()
        );
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

            metrics.recordLostClaim();

            log.warn(
                    "Kafka acknowledged outbox event [{}] of type [{}] for aggregate type [{}] and aggregate [{}] on attempt [{}], but claim [{}] is no longer owned; correlation [{}].",
                    event.id(),
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.attempts() + 1,
                    event.claimId(),
                    event.correlationId()
            );

            return;
        }

        metrics.recordPublished();

        log.info(
                "Published outbox event [{}] of type [{}] for aggregate type [{}] and aggregate [{}] with claim [{}] on attempt [{}]; correlation [{}].",
                event.id(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.claimId(),
                event.attempts() + 1,
                event.correlationId()
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

        metrics.recordConfirmedFailure();
        metrics.recordRetry();

        log.warn(
                "Failed to publish outbox event [{}] of type [{}] for aggregate type [{}] and aggregate [{}] with claim [{}] on attempt [{}]; retrying after [{}], correlation [{}].",
                event.id(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.claimId(),
                failedAttempt,
                retryBackoff,
                event.correlationId(),
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

        metrics.recordConfirmedFailure();
        metrics.recordExhausted();

        log.error(
                "Outbox event [{}] of type [{}] for aggregate type [{}] and aggregate [{}] exhausted [{}] publication attempts with claim [{}] and requires operational recovery; correlation [{}].",
                event.id(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                properties.maxAttempts(),
                event.claimId(),
                event.correlationId(),
                exception
        );
    }

    private void logLostClaimOnFailure(
            ClaimedOutboxEvent event,
            Exception exception
    ) {

        metrics.recordLostClaim();

        log.warn(
                "Unable to record publication failure for outbox event [{}] of type [{}] for aggregate type [{}] and aggregate [{}] on attempt [{}] because claim [{}] is no longer owned; correlation [{}].",
                event.id(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.attempts() + 1,
                event.claimId(),
                event.correlationId(),
                exception
        );
    }
}
