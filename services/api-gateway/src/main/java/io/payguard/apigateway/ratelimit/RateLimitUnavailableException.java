package io.payguard.apigateway.ratelimit;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RateLimitUnavailableException extends ResponseStatusException {

    public RateLimitUnavailableException() {

        super(HttpStatus.SERVICE_UNAVAILABLE, "Request rate limiting is temporarily unavailable.");
    }

    public RateLimitUnavailableException(Throwable cause) {

        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Request rate limiting is temporarily unavailable.",
                cause
        );
    }
}
