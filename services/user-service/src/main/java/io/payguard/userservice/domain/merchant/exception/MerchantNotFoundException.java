package io.payguard.userservice.domain.merchant.exception;

import java.util.UUID;

public class MerchantNotFoundException extends RuntimeException {

    public MerchantNotFoundException(UUID merchantId) {
        super("Merchant '%s' was not found.".formatted(merchantId));
    }

}