package io.payguard.userservice.integration.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.payguard.userservice.integration.identity.exception.KeycloakUnavailableException;
import io.payguard.userservice.integration.payment.error.exception.StripeProviderUnavailableException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Predicate;

@Configuration
@EnableConfigurationProperties(ProviderCircuitBreakerProperties.class)
public class ProviderCircuitBreakerConfiguration {

    private static final String KEYCLOAK_TOKEN = "keycloak-token";
    private static final String KEYCLOAK_ADMIN = "keycloak-admin";
    private static final String STRIPE_API = "stripe-api";

    @Bean
    public CircuitBreakerRegistry providerCircuitBreakerRegistry() {

        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public ProviderCircuitBreakers providerCircuitBreakers(CircuitBreakerRegistry registry, ProviderCircuitBreakerProperties properties) {

        return new ProviderCircuitBreakers(
                registry.circuitBreaker(
                        KEYCLOAK_TOKEN,
                        createConfig(
                                properties,
                                properties.keycloakTokenSlowCallDuration(),
                                KeycloakUnavailableException.class::isInstance
                        )
                ),
                registry.circuitBreaker(
                        KEYCLOAK_ADMIN,
                        createConfig(
                                properties,
                                properties.keycloakAdminSlowCallDuration(),
                                KeycloakUnavailableException.class::isInstance
                        )
                ),
                registry.circuitBreaker(
                        STRIPE_API,
                        createConfig(
                                properties,
                                properties.stripeApiSlowCallDuration(),
                                StripeProviderUnavailableException.class::isInstance
                        )
                )
        );
    }

    @Bean
    public MeterBinder circuitBreakerMetrics(CircuitBreakerRegistry registry) {

        return TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry);
    }

    private CircuitBreakerConfig createConfig(
            ProviderCircuitBreakerProperties properties,
            Duration slowCallDuration,
            Predicate<Throwable> recordException
    ) {

        return CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(properties.slidingWindowSize())
                .minimumNumberOfCalls(properties.minimumNumberOfCalls())
                .failureRateThreshold(properties.failureRateThreshold())
                .slowCallRateThreshold(properties.slowCallRateThreshold())
                .slowCallDurationThreshold(slowCallDuration)
                .permittedNumberOfCallsInHalfOpenState(properties.permittedCallsInHalfOpenState())
                .waitDurationInOpenState(properties.waitDurationInOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(properties.automaticTransitionFromOpenToHalfOpenEnabled())
                .recordException(recordException)
                .ignoreException(recordException.negate())
                .build();
    }
}
