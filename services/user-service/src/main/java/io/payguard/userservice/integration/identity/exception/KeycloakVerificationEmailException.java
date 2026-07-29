package io.payguard.userservice.integration.identity.exception;

public class KeycloakVerificationEmailException
        extends KeycloakException {

    public KeycloakVerificationEmailException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
