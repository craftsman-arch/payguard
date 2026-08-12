package io.payguard.userservice.domain.settlement.event;

import io.payguard.userservice.domain.event.DomainEvent;
import io.payguard.userservice.domain.settlement.SettlementAccountState;
import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.value.UserId;
import lombok.NonNull;

import java.time.Instant;

public record SettlementAccountStateChanged(
        @NonNull SettlementAccountId settlementAccountId,
        @NonNull UserId userId,
        @NonNull SettlementAccountState previousState,
        @NonNull SettlementAccountState currentState,
        @NonNull Instant occurredAt
) implements DomainEvent { }
