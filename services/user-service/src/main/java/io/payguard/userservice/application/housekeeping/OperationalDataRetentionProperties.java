package io.payguard.userservice.application.housekeeping;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.ZoneId;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

@ConfigurationProperties(prefix = "housekeeping.retention")
public record OperationalDataRetentionProperties(

        boolean enabled,
        @NonNull Duration processedStripeEvents,
        @NonNull Duration publishedOutboxEvents,
        @NonNull Duration outboxEventRecoveries,
        int batchSize,
        @NonNull String cron,
        @NonNull String zone

) {

    public OperationalDataRetentionProperties {

        requirePositive(
                processedStripeEvents,
                "Processed Stripe event retention must be positive."
        );

        requirePositive(
                publishedOutboxEvents,
                "Published outbox event retention must be positive."
        );

        requirePositive(
                outboxEventRecoveries,
                "Outbox event recovery retention must be positive."
        );

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "Operational retention batch size must be positive."
            );
        }

        requireText(
                cron,
                "Operational retention cron expression must not be blank."
        );

        requireText(
                zone,
                "Operational retention time zone must not be blank."
        );

        ZoneId.of(zone);
    }

    private static void requirePositive(Duration value, String message) {

        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(message);
        }
    }
}
