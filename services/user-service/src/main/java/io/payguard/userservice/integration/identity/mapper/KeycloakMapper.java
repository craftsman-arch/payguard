package io.payguard.userservice.integration.identity.mapper;

import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.integration.identity.dto.KeycloakCreateUserRequest;
import io.payguard.userservice.integration.identity.dto.KeycloakCredential;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static io.payguard.userservice.integration.identity.KeycloakIdentityOrigin.ATTRIBUTE;
import static io.payguard.userservice.integration.identity.KeycloakIdentityOrigin.USER_SELF_REGISTRATION;

@Component
public class KeycloakMapper {

    private static final String VERIFY_EMAIL = "VERIFY_EMAIL";
    private static final String CONFIGURE_TOTP = "CONFIGURE_TOTP";

    public KeycloakCreateUserRequest toCreateUserRequest(
            Email email,
            String password
    ) {

        return new KeycloakCreateUserRequest(
                email.getValue(),
                email.getValue(),
                true,
                false,
                List.of(
                        new KeycloakCredential(
                                "password",
                                password,
                                false
                        )
                ),
                List.of(
                        VERIFY_EMAIL,
                        CONFIGURE_TOTP
                ),
                Map.of(
                        ATTRIBUTE,
                        List.of(USER_SELF_REGISTRATION)
                )
        );
    }

}
