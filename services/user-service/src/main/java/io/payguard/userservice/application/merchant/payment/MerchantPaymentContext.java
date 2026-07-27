package io.payguard.userservice.application.merchant.payment;

import io.payguard.userservice.domain.merchant.PaymentAccountStatus;

import java.util.UUID;

public record MerchantPaymentContext(
        UUID merchantId,
        String stripeConnectedAccountId,
        PaymentAccountStatus paymentAccountStatus,
        boolean isReadyForPayments,
        boolean eligibleForDestinationCharges
) {
}
