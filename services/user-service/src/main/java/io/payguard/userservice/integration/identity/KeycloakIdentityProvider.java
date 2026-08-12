package io.payguard.userservice.integration.identity;


import io.payguard.userservice.application.identity.IdentityProvider;
import io.payguard.userservice.application.identity.IdentityRole;
import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.integration.identity.client.KeycloakAdminClient;
import io.payguard.userservice.integration.identity.dto.KeycloakUser;
import io.payguard.userservice.integration.identity.mapper.KeycloakMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static io.payguard.userservice.integration.identity.KeycloakIdentityOrigin.ATTRIBUTE;
import static io.payguard.userservice.integration.identity.KeycloakIdentityOrigin.LEGACY_MERCHANT_SELF_REGISTRATION;
import static io.payguard.userservice.integration.identity.KeycloakIdentityOrigin.USER_SELF_REGISTRATION;

@Component
@RequiredArgsConstructor
public class KeycloakIdentityProvider implements IdentityProvider {

    private final KeycloakAdminClient keycloakAdminClient;
    private final KeycloakMapper keycloakMapper;

    @Override
    public String createUser(
            Email email,
            String password
    ) {

        return keycloakAdminClient.createUser(
                keycloakMapper.toCreateUserRequest(email, password)
        );
    }

    @Override
    public void assignRealmRole(String identityUserId, IdentityRole role) {

        keycloakAdminClient.assignRealmRole(
                identityUserId,
                role.value()
        );
    }

    @Override
    public void sendVerificationEmail(String identityUserId) {
        keycloakAdminClient.sendVerificationEmail(identityUserId);
    }

    @Override
    public void resumeUserIdentityRegistration(Email email) {

        keycloakAdminClient.findByEmail(email.getValue())
                .filter(this::isUserSelfRegistration)
                .ifPresent(user -> {

                    keycloakAdminClient.assignRealmRole(
                            user.id(),
                            IdentityRole.USER.value()
                    );

                    if (!user.emailVerified()) {
                        keycloakAdminClient.sendVerificationEmail(
                                user.id()
                        );
                    }
                });
    }

    private boolean isUserSelfRegistration(KeycloakUser user) {

        Map<String, List<String>> attributes = user.attributes();

        if (attributes == null) {
            return false;
        }

        List<String> origins = attributes.getOrDefault(
                ATTRIBUTE,
                List.of()
        );

        return origins.contains(USER_SELF_REGISTRATION)
                || origins.contains(LEGACY_MERCHANT_SELF_REGISTRATION);
    }

}
