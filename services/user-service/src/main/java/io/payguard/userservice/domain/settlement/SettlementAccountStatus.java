package io.payguard.userservice.domain.settlement;

public enum SettlementAccountStatus {

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
