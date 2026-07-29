package io.payguard.userservice.application.merchant.payment;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "payment-provider.account-creation")
public record PaymentAccountCreationProperties(

        @NotNull Duration idempotencySafetyWindow

) {

    public PaymentAccountCreationProperties {

        if (idempotencySafetyWindow != null
                && (
                    idempotencySafetyWindow.isZero()
                    || idempotencySafetyWindow.isNegative()
                )) {

            throw new IllegalArgumentException(
                    "Stripe idempotency safety window must be positive."
            );
        }
    }
}
