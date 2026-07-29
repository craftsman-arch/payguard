package io.payguard.userservice.application.merchant.profile;

import io.payguard.userservice.domain.merchant.MerchantStatus;

import java.util.UUID;

public record CreateMerchantProfileResult(

        UUID id,
        MerchantStatus status,
        boolean created

) {
}
