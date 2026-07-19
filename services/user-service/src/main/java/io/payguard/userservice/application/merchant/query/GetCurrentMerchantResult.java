package io.payguard.userservice.application.merchant.query;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.merchant.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

public record GetCurrentMerchantResult(

        UUID id,
        String email,
        String legalName,
        BusinessType businessType,
        String country,
        MerchantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}