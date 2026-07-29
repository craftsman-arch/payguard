package io.payguard.userservice.integration.identity.exception;

public class KeycloakUnavailableException extends KeycloakException {

    public KeycloakUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
