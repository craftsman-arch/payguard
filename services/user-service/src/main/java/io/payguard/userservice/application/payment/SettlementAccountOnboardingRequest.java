package io.payguard.userservice.application.payment;

import lombok.NonNull;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

public record SettlementAccountOnboardingRequest(
        @NonNull String providerAccountId
) {

    public SettlementAccountOnboardingRequest {
        requireText(
                providerAccountId,
                "Provider account id must not be blank."
        );
    }
}
