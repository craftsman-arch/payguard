package io.payguard.userservice.integration.payment.error.exception;

public class StripeAccountCreationException extends StripeException {

    public StripeAccountCreationException(String message) {
        super(message);
    }

    public StripeAccountCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}