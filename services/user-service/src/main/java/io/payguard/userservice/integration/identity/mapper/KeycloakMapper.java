package io.payguard.userservice.integration.identity.mapper;

import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.identity.dto.KeycloakCreateUserRequest;
import io.payguard.userservice.integration.identity.dto.KeycloakCredential;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakMapper {

    public KeycloakCreateUserRequest toCreateUserRequest(
            Email email,
            String temporaryPassword
    ) {

        return new KeycloakCreateUserRequest(
                email.getValue(),
                email.getValue(),
                true,
                true,
                List.of(
                        new KeycloakCredential(
                                "password",
                                temporaryPassword,
                                true
                        )
                )
        );
    }

}