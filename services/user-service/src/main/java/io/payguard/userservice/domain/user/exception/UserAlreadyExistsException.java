package io.payguard.userservice.domain.user.exception;

import io.payguard.userservice.domain.common.value.Email;

public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException(Email email, Throwable cause) {
        super(
                "User with email '%s' already exists."
                        .formatted(email.getValue()),
                cause
        );
    }
}
