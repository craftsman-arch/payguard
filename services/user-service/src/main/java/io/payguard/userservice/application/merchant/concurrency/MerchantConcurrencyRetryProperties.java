package io.payguard.userservice.application.merchant.concurrency;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

import static io.payguard.userservice.common.validation.DurationPreconditions.requirePositive;

@Validated
@ConfigurationProperties(prefix = "merchant.concurrency-retry")
public record MerchantConcurrencyRetryProperties(

        @Min(1)
        int maxAttempts,

        @NotNull
        Duration minBackoff,

        @NotNull
        Duration maxBackoff

) {

    public MerchantConcurrencyRetryProperties {

        requirePositive(
                minBackoff,
                "Minimum concurrency retry backoff must be positive."
        );
        requirePositive(
                maxBackoff,
                "Maximum concurrency retry backoff must be positive."
        );

        if (minBackoff.compareTo(maxBackoff) > 0) {

            throw new IllegalArgumentException(
                    "Minimum concurrency retry backoff must not exceed maximum backoff."
            );
        }
    }
}
