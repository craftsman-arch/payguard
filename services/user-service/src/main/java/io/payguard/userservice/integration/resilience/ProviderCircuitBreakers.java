package io.payguard.userservice.integration.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public record ProviderCircuitBreakers(

        CircuitBreaker keycloakToken,
        CircuitBreaker keycloakAdmin,
        CircuitBreaker stripeApi

) {
}
