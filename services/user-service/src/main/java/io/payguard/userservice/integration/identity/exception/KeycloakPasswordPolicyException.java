package io.payguard.userservice.integration.identity.exception;

public class KeycloakPasswordPolicyException extends KeycloakException {

    public KeycloakPasswordPolicyException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
