package io.payguard.userservice.domain.merchant.exception;

public class MerchantNotReadyForActivationException extends MerchantException {

    public MerchantNotReadyForActivationException() {
        super("Merchant cannot be activated until identity provider and payment provider are linked.");
    }

}