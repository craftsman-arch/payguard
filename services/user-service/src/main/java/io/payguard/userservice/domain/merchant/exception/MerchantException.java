package io.payguard.userservice.domain.merchant.exception;

public abstract class MerchantException extends RuntimeException {

    protected MerchantException(String message) {
        super(message);
    }

}