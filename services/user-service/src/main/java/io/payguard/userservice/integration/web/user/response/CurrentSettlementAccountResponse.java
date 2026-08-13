package io.payguard.userservice.integration.web.user.response;

import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.payguard.userservice.domain.settlement.SettlementAccountRequiredAction;
import io.payguard.userservice.domain.settlement.SettlementAccountStatus;
import io.payguard.userservice.domain.settlement.SettlementProvider;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Current user's settlement account.")
public record CurrentSettlementAccountResponse(
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
