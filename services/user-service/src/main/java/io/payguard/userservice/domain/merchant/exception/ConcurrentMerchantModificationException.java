package io.payguard.userservice.domain.merchant.exception;

import java.util.UUID;

public class ConcurrentMerchantModificationException extends MerchantException {

    public ConcurrentMerchantModificationException(UUID merchantId, Throwable cause) {

        super("Merchant [%s] was concurrently modified.".formatted(merchantId), cause);
    }
}
