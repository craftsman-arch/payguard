package io.payguard.userservice.domain.merchant.exception;

public class UnverifiedMerchantIdentityException extends MerchantException {

    public UnverifiedMerchantIdentityException() {
        super("Merchant email must be verified.");
    }
}
