package io.payguard.userservice.application.merchant.query;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.merchant.MerchantStatus;
import io.payguard.userservice.domain.merchant.PaymentAccountRequiredAction;
import io.payguard.userservice.domain.merchant.PaymentAccountStatus;

import java.time.Instant;
import java.util.UUID;

public record CurrentMerchantResult(

        UUID id,
        String email,
        String legalName,
        BusinessType businessType,
        String country,
        MerchantStatus status,
        PaymentAccountStatus paymentAccountStatus,
        String paymentAccountStatusReason,
        PaymentAccountRequiredAction paymentAccountRequiredAction,
        boolean isReadyForPayments,
        Instant createdAt,
        Instant updatedAt
) {
}
