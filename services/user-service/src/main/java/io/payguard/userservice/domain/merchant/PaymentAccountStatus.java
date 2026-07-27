package io.payguard.userservice.domain.merchant;

public enum PaymentAccountStatus {

    PENDING_ONBOARDING,
    ACTIVE,
    RESTRICTED,
    DISABLED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }
}