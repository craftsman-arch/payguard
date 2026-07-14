package io.payguard.userservice.domain.merchant.exception;

public class MerchantAlreadySuspendedException extends MerchantException {

    public MerchantAlreadySuspendedException() {
        super("Merchant is already suspended.");
    }

}