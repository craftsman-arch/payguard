package io.payguard.userservice.integration.event.outbox;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static io.payguard.userservice.common.validation.DurationPreconditions.requirePositive;
import static io.payguard.userservice.common.validation.NumberPreconditions.requirePositive;
import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

@ConfigurationProperties(prefix = "outbox.publisher")
public record OutboxPublisherProperties(

        boolean enabled,
        @NonNull String topic,
        int topicPartitions,
        int topicReplicas,
        int batchSize,
        @NonNull Duration pollInterval,
        @NonNull Duration leaseDuration,
        @NonNull Duration sendTimeout,
        @NonNull Duration baseBackoff,
        @NonNull Duration maxBackoff,
        int maxAttempts

) {

    public OutboxPublisherProperties {

        requireText(
                topic,
                "Outbox Kafka topic must not be blank."
        );

        requirePositive(
                topicPartitions,
                "Outbox Kafka topic partitions must be positive."
        );

        requirePositive(
                topicReplicas,
                "Outbox Kafka topic replicas must be positive."
        );

        requirePositive(
                batchSize,
                "Outbox publisher batch size must be positive."
        );

        requirePositive(
                maxAttempts,
                "Outbox publisher max attempts must be positive."
        );

        requirePositive(
                pollInterval,
                "Outbox publisher poll interval must be positive."
        );

        requirePositive(
                leaseDuration,
                "Outbox publisher lease duration must be positive."
        );

        requirePositive(
                sendTimeout,
                "Outbox publisher send timeout must be positive."
        );

        requirePositive(
                baseBackoff,
                "Outbox publisher base backoff must be positive."
        );

        requirePositive(
                maxBackoff,
                "Outbox publisher max backoff must be positive."
        );

        if (baseBackoff.compareTo(maxBackoff) > 0) {
            throw new IllegalArgumentException(
                    "Outbox publisher base backoff must not exceed max backoff."
            );
        }

        Duration maximumBatchSendTime =
                sendTimeout.multipliedBy(batchSize);

        if (leaseDuration.compareTo(maximumBatchSendTime) <= 0) {
            throw new IllegalArgumentException(
                    "Outbox publisher lease duration must exceed batch size multiplied by send timeout."
            );
        }
    }

}
