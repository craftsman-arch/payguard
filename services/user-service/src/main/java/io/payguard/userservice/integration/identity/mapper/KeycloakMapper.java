package io.payguard.userservice.integration.identity.mapper;

import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.identity.dto.KeycloakCreateUserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KeycloakMapper {

    KeycloakCreateUserRequest toCreateUserRequest(
            Email email,
            String temporaryPassword
    );

}