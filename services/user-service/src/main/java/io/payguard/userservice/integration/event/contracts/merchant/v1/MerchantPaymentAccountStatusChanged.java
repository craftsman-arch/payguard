package io.payguard.userservice.integration.event.contracts.merchant.v1;

import lombok.NonNull;

import java.util.UUID;

public record MerchantPaymentAccountStatusChanged(

        @NonNull UUID merchantId,
        @NonNull PaymentState previousState,
        @NonNull PaymentState currentState

) {

    public static final String TYPE = "merchant.payment-account-status-changed.v1";

    public record PaymentState(

            @NonNull PaymentAccountStatus paymentAccountStatus,
            @NonNull PaymentAccountRequiredAction requiredAction,
            boolean readyForPayments,
            boolean eligibleForDestinationCharges

    ) { }

    public enum PaymentAccountStatus {

        PENDING_ONBOARDING,
        ACTIVE,
        RESTRICTED,
        DISABLED
    }

    public enum PaymentAccountRequiredAction {

        CONTINUE_ONBOARDING,
        WAIT_FOR_REVIEW,
        CONTACT_SUPPORT,
        NONE
    }
}
