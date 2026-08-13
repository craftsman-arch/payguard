package io.payguard.userservice.application.user.query;

import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.payguard.userservice.domain.settlement.SettlementAccountRequiredAction;
import io.payguard.userservice.domain.settlement.SettlementAccountStatus;
import io.payguard.userservice.domain.settlement.SettlementProvider;

import java.time.Instant;
import java.util.UUID;

public record CurrentSettlementAccountResult(
        UUID id,
        SettlementProvider provider,
        String providerAccountId,
        String accountHolderName,
        SettlementAccountHolderType accountHolderType,
        String country,
        SettlementAccountStatus status,
        String statusReason,
        SettlementAccountRequiredAction requiredAction,
        boolean canReceiveTransfers,
        Instant createdAt,
        Instant updatedAt
) {
}
