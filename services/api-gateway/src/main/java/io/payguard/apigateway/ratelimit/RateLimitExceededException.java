package io.payguard.apigateway.ratelimit;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RateLimitExceededException extends ResponseStatusException {

    public RateLimitExceededException() {

        super(HttpStatus.TOO_MANY_REQUESTS, "Request rate limit exceeded.");
    }
}
