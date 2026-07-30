package io.payguard.userservice.integration.http;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

public record HttpClientTimeouts(

        Duration connectTimeout,
        Duration readTimeout

) {

    public HttpClientTimeouts {

        requirePositive(connectTimeout, "HTTP connect timeout");
        requirePositive(readTimeout, "HTTP read timeout");
    }

    private static void requirePositive(Duration duration, String valueName) {

        requireNonNull(
                duration,
                "%s must be configured.".formatted(valueName)
        );

        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(
                    "%s must be positive.".formatted(valueName)
            );
        }
    }
}
