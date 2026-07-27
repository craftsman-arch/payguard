package io.payguard.userservice.domain.merchant;

import lombok.NonNull;

public enum PaymentAccountRequiredAction {

    CONTINUE_ONBOARDING,
    WAIT_FOR_REVIEW,
    CONTACT_SUPPORT,
    NONE;

    public static PaymentAccountRequiredAction resolve(
            @NonNull PaymentAccountStatus status,
            @NonNull PaymentAccountRequirements requirements
    ) {

        if (status.isDisabled()) {
            return CONTACT_SUPPORT;
        }

        if (status.isActive()) {
            return NONE;
        }

        if (requirements.requiresMerchantAction()) {
            return CONTINUE_ONBOARDING;
        }

        if (requirements.isPendingVerification()) {
            return WAIT_FOR_REVIEW;
        }

        return CONTINUE_ONBOARDING;
    }

    public boolean requiresOnboarding() {
        return this == CONTINUE_ONBOARDING;
    }
}