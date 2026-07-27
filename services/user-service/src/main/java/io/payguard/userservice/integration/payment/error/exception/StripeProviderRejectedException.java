package io.payguard.userservice.integration.payment.error.exception;

public class StripeProviderRejectedException extends StripeException {

    public StripeProviderRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}