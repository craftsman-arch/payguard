package io.payguard.userservice.integration.payment.error.exception;

public class StripeEventDeserializationException extends StripeException {

    public StripeEventDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

}