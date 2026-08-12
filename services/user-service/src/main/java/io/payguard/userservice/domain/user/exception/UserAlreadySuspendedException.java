package io.payguard.userservice.domain.user.exception;

public class UserAlreadySuspendedException extends UserException {

    public UserAlreadySuspendedException() {
        super("User is already suspended.");
    }
}
