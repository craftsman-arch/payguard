package io.payguard.userservice.integration.identity.dto;

public record KeycloakUser(

        String id,
        String username,
        String email

) {
}