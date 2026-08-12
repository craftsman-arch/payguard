package io.payguard.userservice.domain.user.exception;

public class InvalidUserRevisionException extends UserException {

    public InvalidUserRevisionException(long revision) {
        super("User revision must not be negative: " + revision);
    }
}
