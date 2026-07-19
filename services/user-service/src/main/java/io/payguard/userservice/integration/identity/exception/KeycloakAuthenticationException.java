package io.payguard.userservice.integration.identity.exception;

public class KeycloakAuthenticationException extends KeycloakException {

    public KeycloakAuthenticationException(String message) {
        super(message);
    }

    public KeycloakAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}