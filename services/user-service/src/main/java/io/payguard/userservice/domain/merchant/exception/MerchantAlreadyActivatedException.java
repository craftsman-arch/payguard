package io.payguard.userservice.domain.merchant.exception;

public class MerchantAlreadyActivatedException extends MerchantException {

    public MerchantAlreadyActivatedException() {
        super("Merchant is already active.");
    }

}