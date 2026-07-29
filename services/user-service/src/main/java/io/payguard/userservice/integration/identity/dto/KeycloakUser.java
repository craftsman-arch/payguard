package io.payguard.userservice.integration.identity.dto;

import java.util.List;
import java.util.Map;

public record KeycloakUser(

        String id,
        String username,
        String email,
        boolean emailVerified,
        Map<String, List<String>> attributes

) {
}
