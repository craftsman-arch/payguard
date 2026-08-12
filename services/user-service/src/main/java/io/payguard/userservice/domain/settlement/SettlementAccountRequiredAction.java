package io.payguard.userservice.domain.settlement;

import lombok.NonNull;

public enum SettlementAccountRequiredAction {

    CONTINUE_ONBOARDING,
    WAIT_FOR_REVIEW,
    CONTACT_SUPPORT,
    NONE;

    public static SettlementAccountRequiredAction resolve(
            @NonNull SettlementAccountStatus status,
            @NonNull SettlementAccountRequirements requirements
    ) {

        if (status.isDisabled()) {
            return CONTACT_SUPPORT;
        }

        if (status.isActive()) {
            return NONE;
        }

        if (requirements.requiresUserAction()) {
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
