package io.payguard.userservice.domain.settlement;

import io.payguard.userservice.domain.event.DomainEvent;
import io.payguard.userservice.domain.settlement.event.SettlementAccountStateChanged;
import lombok.NonNull;

import java.util.List;

public sealed interface SettlementAccountUpdateResult
        permits SettlementAccountUpdateResult.Stale,
                SettlementAccountUpdateResult.AppliedWithoutStateChange,
                SettlementAccountUpdateResult.StateChanged {

    boolean wasApplied();

    default List<DomainEvent> domainEvents() {
        return List.of();
    }

    record Stale() implements SettlementAccountUpdateResult {

        @Override
        public boolean wasApplied() {
            return false;
        }
    }

    record AppliedWithoutStateChange() implements SettlementAccountUpdateResult {

        @Override
        public boolean wasApplied() {
            return true;
        }
    }

    record StateChanged(@NonNull SettlementAccountStateChanged event) implements SettlementAccountUpdateResult {

        @Override
        public boolean wasApplied() {
            return true;
        }

        @Override
        public List<DomainEvent> domainEvents() {
            return List.of(event);
        }
    }
}
