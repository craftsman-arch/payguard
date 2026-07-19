package io.payguard.userservice.integration.payment.error.exception;

public class StripeSignatureVerificationException extends StripeException {

    public StripeSignatureVerificationException(String message) {
        super(message);
    }

}