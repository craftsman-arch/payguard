package io.payguard.userservice.domain.merchant.event;

import io.payguard.userservice.domain.event.DomainEvent;
import io.payguard.userservice.domain.merchant.MerchantPaymentState;
import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

public record MerchantPaymentStateChanged(

        @NonNull UUID merchantId,
        @NonNull MerchantPaymentState previousState,
        @NonNull MerchantPaymentState currentState,
        @NonNull Instant occurredAt

) implements DomainEvent {
}
