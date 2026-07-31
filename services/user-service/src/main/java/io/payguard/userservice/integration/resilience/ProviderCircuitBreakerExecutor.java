package io.payguard.userservice.integration.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.payguard.userservice.integration.identity.exception.KeycloakUnavailableException;
import io.payguard.userservice.integration.payment.error.exception.StripeProviderUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderCircuitBreakerExecutor {

    private final ProviderCircuitBreakers circuitBreakers;

    public <T> T executeKeycloakToken(Supplier<T> operation) {

        try {
            return circuitBreakers
                    .keycloakToken()
                    .executeSupplier(operation);

        } catch (CallNotPermittedException exception) {

            logRejectedCall(circuitBreakers.keycloakToken());

            throw new KeycloakUnavailableException(
                    "Keycloak token circuit is open.",
                    exception
            );
        }
    }

    public <T> T executeKeycloakAdmin(Supplier<T> operation) {

        try {
            return circuitBreakers
                    .keycloakAdmin()
                    .executeSupplier(operation);

        } catch (CallNotPermittedException exception) {

            logRejectedCall(circuitBreakers.keycloakAdmin());

            throw new KeycloakUnavailableException(
                    "Keycloak Admin circuit is open.",
                    exception
            );
        }
    }

    public void executeKeycloakAdmin(Runnable operation) {

        executeKeycloakAdmin(() -> {
            operation.run();
            return null;
        });
    }

    public <T> T executeStripe(Supplier<T> operation) {

        try {
            return circuitBreakers
                    .stripeApi()
                    .executeSupplier(operation);

        } catch (CallNotPermittedException exception) {

            logRejectedCall(circuitBreakers.stripeApi());

            throw new StripeProviderUnavailableException(
                    "Stripe API circuit is open.",
                    exception
            );
        }
    }

    private void logRejectedCall(CircuitBreaker circuitBreaker) {

        log.warn(
                "Provider circuit [{}] rejected a call while in state [{}].",
                circuitBreaker.getName(),
                circuitBreaker.getState()
        );
    }
}
