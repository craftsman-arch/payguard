package io.payguard.userservice.integration.identity.exception;

public class KeycloakUserCreationException extends KeycloakException {

    public KeycloakUserCreationException(String message) {
        super(message);
    }

    public KeycloakUserCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}