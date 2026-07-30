package io.payguard.userservice.integration.payment.error.exception;

public class StripeProviderResponseException extends StripeException {

    public StripeProviderResponseException(String message) {
        super(message);
    }

    public StripeProviderResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
