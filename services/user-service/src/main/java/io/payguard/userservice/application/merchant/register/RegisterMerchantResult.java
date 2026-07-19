package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.domain.merchant.MerchantStatus;

import java.util.UUID;

public record RegisterMerchantResult(

        UUID id,
        MerchantStatus status,
        String onboardingUrl

) {
}