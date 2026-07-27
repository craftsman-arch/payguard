package io.payguard.userservice.domain.merchant;

import io.payguard.userservice.domain.event.DomainEvent;
import io.payguard.userservice.domain.merchant.event.MerchantPaymentStateChanged;
import lombok.NonNull;

import java.util.List;

public sealed interface PaymentAccountUpdateResult
        permits PaymentAccountUpdateResult.Stale,
                PaymentAccountUpdateResult.AppliedWithoutStateChange,
                PaymentAccountUpdateResult.StateChanged {

    boolean wasApplied();

    default List<DomainEvent> domainEvents() {
        return List.of();
    }

    record Stale() implements PaymentAccountUpdateResult {

        @Override
        public boolean wasApplied() {
            return false;
        }
    }

    record AppliedWithoutStateChange() implements PaymentAccountUpdateResult {

        @Override
        public boolean wasApplied() {
            return true;
        }
    }

    record StateChanged(@NonNull MerchantPaymentStateChanged event) implements PaymentAccountUpdateResult {

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
