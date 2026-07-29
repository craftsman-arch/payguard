package io.payguard.userservice.integration.identity.client;

import io.payguard.userservice.integration.identity.dto.KeycloakCreateUserRequest;
import io.payguard.userservice.integration.identity.dto.KeycloakUser;

import java.util.Optional;

public interface KeycloakAdminClient {

    String createUser(KeycloakCreateUserRequest request);

    Optional<KeycloakUser> findByEmail(String email);

    void assignRealmRole(String userId, String roleName);

    void sendVerificationEmail(String userId);

}
