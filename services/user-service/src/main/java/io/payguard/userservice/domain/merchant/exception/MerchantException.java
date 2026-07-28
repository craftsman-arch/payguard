package io.payguard.userservice.domain.merchant.exception;

public abstract class MerchantException extends RuntimeException {

    protected MerchantException(String message) {
        super(message);
    }

    protected MerchantException(String message, Throwable cause) {
        super(message, cause);
    }

}
