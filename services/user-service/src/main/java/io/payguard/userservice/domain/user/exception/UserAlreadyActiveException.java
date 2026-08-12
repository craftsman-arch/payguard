package io.payguard.userservice.domain.user.exception;

public class UserAlreadyActiveException extends UserException {

    public UserAlreadyActiveException() {
        super("User is already active.");
    }
}
