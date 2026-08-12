package io.payguard.userservice.domain.settlement;

import lombok.NonNull;

public record SettlementAccountState(
        @NonNull SettlementAccountStatus status,
        @NonNull SettlementAccountRequiredAction requiredAction,
        boolean canReceiveTransfers
) {

    public boolean hasChangedSince(@NonNull SettlementAccountState previous) {
        return !equals(previous);
    }
}
