package io.payguard.userservice.application.user.profile;

import io.payguard.userservice.domain.user.UserStatus;

import java.util.UUID;

public record CreateUserProfileResult(
        UUID id,
        UserStatus status,
        boolean created
) {
}
