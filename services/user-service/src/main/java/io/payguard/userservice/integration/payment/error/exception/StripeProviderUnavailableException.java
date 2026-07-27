package io.payguard.userservice.integration.payment.error.exception;

public class StripeProviderUnavailableException extends StripeException {

    public StripeProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}