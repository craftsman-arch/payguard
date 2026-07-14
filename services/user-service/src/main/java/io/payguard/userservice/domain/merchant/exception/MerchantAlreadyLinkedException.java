package io.payguard.userservice.domain.merchant.exception;

public class MerchantAlreadyLinkedException extends MerchantException {

    public MerchantAlreadyLinkedException(String target) {
        super("Merchant is already linked to " + target + ".");
    }

}