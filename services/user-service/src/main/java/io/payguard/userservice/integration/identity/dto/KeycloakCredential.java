package io.payguard.userservice.integration.identity.dto;

public record KeycloakCredential(

        String type,
        String value,
        boolean temporary
) {
}