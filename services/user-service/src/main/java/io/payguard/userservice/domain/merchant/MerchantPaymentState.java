package io.payguard.userservice.domain.merchant;

import lombok.NonNull;

public record MerchantPaymentState(

        @NonNull PaymentAccountStatus paymentAccountStatus,
        @NonNull PaymentAccountRequiredAction requiredAction,
        boolean readyForPayments,
        boolean eligibleForDestinationCharges

) {

    public boolean hasChangedSince(@NonNull MerchantPaymentState previousState) {

        return !equals(previousState);
    }
}
