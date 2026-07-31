package io.payguard.userservice.integration.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

import static io.payguard.userservice.common.validation.DurationPreconditions.requirePositive;
import static io.payguard.userservice.common.validation.NumberPreconditions.requirePercentage;
import static io.payguard.userservice.common.validation.NumberPreconditions.requirePositive;

@ConfigurationProperties(prefix = "provider-resilience.circuit-breaker")
public record ProviderCircuitBreakerProperties(

        int slidingWindowSize,
        int minimumNumberOfCalls,
        float failureRateThreshold,
        float slowCallRateThreshold,
        int permittedCallsInHalfOpenState,
        Duration waitDurationInOpenState,
        boolean automaticTransitionFromOpenToHalfOpenEnabled,
        Duration keycloakTokenSlowCallDuration,
        Duration keycloakAdminSlowCallDuration,
        Duration stripeApiSlowCallDuration

) {

    public ProviderCircuitBreakerProperties {

        requirePositive(
                slidingWindowSize,
                "Sliding-window size must be positive."
        );
        requirePositive(
                minimumNumberOfCalls,
                "Minimum number of calls must be positive."
        );
        requirePercentage(
                failureRateThreshold,
                "Failure-rate threshold must be greater than 0 and not exceed 100."
        );
        requirePercentage(
                slowCallRateThreshold,
                "Slow-call-rate threshold must be greater than 0 and not exceed 100."
        );
        requirePositive(
                permittedCallsInHalfOpenState,
                "Permitted calls in half-open state must be positive."
        );
        requirePositive(
                waitDurationInOpenState,
                "Open-state wait duration must be positive."
        );
        requirePositive(
                keycloakTokenSlowCallDuration,
                "Keycloak token slow-call duration must be positive."
        );
        requirePositive(
                keycloakAdminSlowCallDuration,
                "Keycloak Admin slow-call duration must be positive."
        );
        requirePositive(
                stripeApiSlowCallDuration,
                "Stripe API slow-call duration must be positive."
        );

        if (minimumNumberOfCalls > slidingWindowSize) {
            throw new IllegalArgumentException(
                    "Minimum number of calls must not exceed the sliding-window size."
            );
        }
    }

}
