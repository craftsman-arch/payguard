package io.payguard.userservice.integration.identity;


import io.payguard.userservice.application.identity.IdentityProvider;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.identity.client.KeycloakAdminClient;
import io.payguard.userservice.integration.identity.mapper.KeycloakMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakIdentityProvider implements IdentityProvider {

    private final KeycloakAdminClient keycloakAdminClient;
    private final KeycloakMapper keycloakMapper;

    @Override
    public String createUser(
            Email email,
            String temporaryPassword
    ) {

        return keycloakAdminClient.createUser(
                keycloakMapper.toCreateUserRequest(email, temporaryPassword)
        );
    }

    @Override
    public void deleteUser(String identityUserId) {
        keycloakAdminClient.deleteUser(identityUserId);
    }

}