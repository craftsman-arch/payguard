package io.payguard.userservice.application.payment;

import lombok.NonNull;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

public record SettlementAccountProvisioningRequest(
        @NonNull String idempotencyKey,
        @NonNull String email,
        @NonNull String country,
        @NonNull String accountHolderType
) {

    public SettlementAccountProvisioningRequest {
        requireText(idempotencyKey, "Idempotency key must not be blank.");
        requireText(email, "Email must not be blank.");
        requireText(country, "Country must not be blank.");
        requireText(accountHolderType, "Account holder type must not be blank.");
    }
}
