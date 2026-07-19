package io.payguard.userservice.domain.merchant.exception;

public class MerchantIdentityNotLinkedException extends MerchantException {

    public MerchantIdentityNotLinkedException() {
        super("Merchant identity is not linked.");
    }

}