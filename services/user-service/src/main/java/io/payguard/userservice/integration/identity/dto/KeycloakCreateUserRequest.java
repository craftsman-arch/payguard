package io.payguard.userservice.integration.identity.dto;

import java.util.List;

public record KeycloakCreateUserRequest(

        String username,
        String email,
        boolean enabled,
        boolean emailVerified,
        List<KeycloakCredential> credentials
) {
}