package io.payguard.userservice.integration.identity.exception;

public abstract class KeycloakException extends RuntimeException {

    protected KeycloakException(String message) {
        super(message);
    }

    protected KeycloakException(String message, Throwable cause) {
        super(message, cause);
    }

}