package io.payguard.userservice.integration.event.outbox;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static io.payguard.userservice.common.validation.DurationPreconditions.requireNotNegative;
import static io.payguard.userservice.common.validation.DurationPreconditions.requirePositive;

@ConfigurationProperties(prefix = "outbox.observability")
public record OutboxObservabilityProperties(

        boolean enabled,
        @NonNull Duration refreshInterval,
        @NonNull Duration initialDelay

) {

    public OutboxObservabilityProperties {

        requirePositive(
                refreshInterval,
                "Outbox observability refresh interval must be positive."
        );
        requireNotNegative(
                initialDelay,
                "Outbox observability initial delay must not be negative."
        );
    }
}
