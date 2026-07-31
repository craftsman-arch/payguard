package io.payguard.userservice.application.housekeeping;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.ZoneId;

import static io.payguard.userservice.common.validation.DurationPreconditions.requirePositive;
import static io.payguard.userservice.common.validation.NumberPreconditions.requirePositive;
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

        requirePositive(
                batchSize,
                "Operational retention batch size must be positive."
        );

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

}
