package io.payguard.userservice.domain.common.exception;

public abstract class DomainValueException extends RuntimeException {

    protected DomainValueException(String message) {
        super(message);
    }
}
