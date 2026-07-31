package io.payguard.userservice.integration.http;

import java.time.Duration;

import static io.payguard.userservice.common.validation.DurationPreconditions.requirePositive;

public record HttpClientTimeouts(

        Duration connectTimeout,
        Duration readTimeout

) {

    public HttpClientTimeouts {

        requirePositive(
                connectTimeout,
                "HTTP connect timeout must be positive."
        );
        requirePositive(
                readTimeout,
                "HTTP read timeout must be positive."
        );
    }
}
