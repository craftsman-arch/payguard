package io.payguard.userservice.application.merchant.payment;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

import static io.payguard.userservice.common.validation.DurationPreconditions.requirePositive;

@Validated
@ConfigurationProperties(prefix = "payment-provider.account-creation")
public record PaymentAccountCreationProperties(

        @NotNull Duration idempotencySafetyWindow

) {

    public PaymentAccountCreationProperties {

        requirePositive(
                idempotencySafetyWindow,
                "Stripe idempotency safety window must be positive."
        );
    }
}
