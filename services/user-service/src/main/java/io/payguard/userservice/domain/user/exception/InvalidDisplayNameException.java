package io.payguard.userservice.domain.user.exception;

public class InvalidDisplayNameException extends UserException {

    public InvalidDisplayNameException() {
        super("Display name must contain between 1 and 100 characters.");
    }
}
