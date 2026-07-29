package io.payguard.userservice.integration.identity;


import io.payguard.userservice.application.identity.IdentityProvider;
import io.payguard.userservice.application.identity.IdentityRole;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.identity.client.KeycloakAdminClient;
import io.payguard.userservice.integration.identity.dto.KeycloakUser;
import io.payguard.userservice.integration.identity.mapper.KeycloakMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static io.payguard.userservice.integration.identity.KeycloakIdentityOrigin.ATTRIBUTE;
import static io.payguard.userservice.integration.identity.KeycloakIdentityOrigin.MERCHANT_SELF_REGISTRATION;

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
    public void resumeMerchantIdentityRegistration(Email email) {

        keycloakAdminClient.findByEmail(email.getValue())
                .filter(this::isMerchantSelfRegistration)
                .ifPresent(user -> {

                    keycloakAdminClient.assignRealmRole(
                            user.id(),
                            IdentityRole.MERCHANT.value()
                    );

                    if (!user.emailVerified()) {
                        keycloakAdminClient.sendVerificationEmail(
                                user.id()
                        );
                    }
                });
    }

    private boolean isMerchantSelfRegistration(KeycloakUser user) {

        Map<String, List<String>> attributes = user.attributes();

        return attributes != null
                && attributes
                        .getOrDefault(ATTRIBUTE, List.of())
                        .contains(MERCHANT_SELF_REGISTRATION);
    }

}
