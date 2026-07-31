package io.payguard.userservice.integration.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderCircuitBreakerStateLogger {

    private final ProviderCircuitBreakers circuitBreakers;

    @PostConstruct
    void subscribeToStateTransitions() {

        subscribe(circuitBreakers.keycloakToken());
        subscribe(circuitBreakers.keycloakAdmin());
        subscribe(circuitBreakers.stripeApi());
    }

    private void subscribe(CircuitBreaker circuitBreaker) {

        circuitBreaker
                .getEventPublisher()
                .onStateTransition(event ->
                        log.warn(
                                "Provider circuit [{}] transitioned from [{}] to [{}].",
                                circuitBreaker.getName(),
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()
                        )
                );
    }
}
