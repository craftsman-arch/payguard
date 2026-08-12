package io.payguard.userservice.domain.user.exception;

public abstract class UserException extends RuntimeException {

    protected UserException(String message) {
        super(message);
    }
}
