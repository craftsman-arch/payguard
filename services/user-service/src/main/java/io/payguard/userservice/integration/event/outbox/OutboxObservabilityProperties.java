package io.payguard.userservice.integration.event.outbox;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "outbox.observability")
public record OutboxObservabilityProperties(

        boolean enabled,
        @NonNull Duration refreshInterval,
        @NonNull Duration initialDelay

) {

    public OutboxObservabilityProperties {

        if (refreshInterval.isZero() || refreshInterval.isNegative()) {

            throw new IllegalArgumentException(
                    "Outbox observability refresh interval must be positive."
            );
        }

        if (initialDelay.isNegative()) {
            throw new IllegalArgumentException(
                    "Outbox observability initial delay must not be negative."
            );
        }
    }
}
