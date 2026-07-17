package io.payguard.userservice.integration.web.merchant.response;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.merchant.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

public record GetCurrentMerchantResponse(
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