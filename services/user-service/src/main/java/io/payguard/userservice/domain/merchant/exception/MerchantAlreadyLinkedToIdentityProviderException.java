package io.payguard.userservice.domain.merchant.exception;

public class MerchantAlreadyLinkedToIdentityProviderException extends MerchantException {

    public MerchantAlreadyLinkedToIdentityProviderException() {
        super("Merchant is already linked to an identity provider.");
    }

}
