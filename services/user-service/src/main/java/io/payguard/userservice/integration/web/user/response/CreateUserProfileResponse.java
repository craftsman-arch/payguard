package io.payguard.userservice.integration.web.user.response;

import io.payguard.userservice.domain.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "User profile creation result.")
public record CreateUserProfileResponse(
        UUID id,
        UserStatus status
) {
}
