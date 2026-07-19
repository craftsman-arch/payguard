package io.payguard.userservice.integration.identity.exception;

public class KeycloakRoleAssignmentException
        extends KeycloakException {

    public KeycloakRoleAssignmentException(String message) {
        super(message);
    }

    public KeycloakRoleAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }

}