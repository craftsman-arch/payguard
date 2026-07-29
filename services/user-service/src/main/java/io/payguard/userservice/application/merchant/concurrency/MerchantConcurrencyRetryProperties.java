package io.payguard.userservice.application.merchant.concurrency;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

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

        if (minBackoff != null
                && (
                    minBackoff.isZero()
                    || minBackoff.isNegative()
                )) {

            throw new IllegalArgumentException(
                    "Minimum concurrency retry backoff must be positive."
            );
        }

        if (maxBackoff != null
                && (
                    maxBackoff.isZero()
                    || maxBackoff.isNegative()
                )) {

            throw new IllegalArgumentException(
                    "Maximum concurrency retry backoff must be positive."
            );
        }

        if (minBackoff != null
                && maxBackoff != null
                && minBackoff.compareTo(maxBackoff) > 0) {

            throw new IllegalArgumentException(
                    "Minimum concurrency retry backoff must not exceed maximum backoff."
            );
        }
    }
}
