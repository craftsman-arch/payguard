package io.payguard.userservice.integration.payment.error.exception;

public class StripeAccountDeletionException extends StripeException {

    public StripeAccountDeletionException(String message) {
        super(message);
    }

    public StripeAccountDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

}