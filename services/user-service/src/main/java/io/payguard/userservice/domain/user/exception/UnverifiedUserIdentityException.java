package io.payguard.userservice.domain.user.exception;

public class UnverifiedUserIdentityException extends UserException {

    public UnverifiedUserIdentityException() {
        super("Email verification is required before creating a user profile.");
    }
}
