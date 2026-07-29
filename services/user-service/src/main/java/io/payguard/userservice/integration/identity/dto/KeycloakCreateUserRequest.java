package io.payguard.userservice.integration.identity.dto;

import java.util.List;
import java.util.Map;

public record KeycloakCreateUserRequest(

        String username,
        String email,
        boolean enabled,
        boolean emailVerified,
        List<KeycloakCredential> credentials,
        List<String> requiredActions,
        Map<String, List<String>> attributes
) {
}
