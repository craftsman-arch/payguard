package io.payguard.userservice.integration.identity.exception;

public class KeycloakUserDeletionException extends KeycloakException {

    public KeycloakUserDeletionException(String message) {
        super(message);
    }

    public KeycloakUserDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

}