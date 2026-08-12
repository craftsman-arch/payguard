package io.payguard.userservice.domain.common.exception;

public class InvalidEmailException extends DomainValueException {

    public InvalidEmailException() {
        super("Email cannot be null or blank.");
    }

    public InvalidEmailException(String email) {
        super("Invalid email address: " + email);
    }

}
