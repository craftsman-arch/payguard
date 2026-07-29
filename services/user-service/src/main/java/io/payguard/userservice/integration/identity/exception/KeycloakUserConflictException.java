package io.payguard.userservice.integration.identity.exception;

public class KeycloakUserConflictException extends KeycloakException {

    public KeycloakUserConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
