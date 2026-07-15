package io.payguard.userservice.domain.merchant.exception;

public class MerchantMustBeActiveException extends MerchantException {

    public MerchantMustBeActiveException() {
        super("Merchant must be active.");
    }

}