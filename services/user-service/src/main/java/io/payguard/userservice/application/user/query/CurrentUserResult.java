package io.payguard.userservice.application.user.query;

import io.payguard.userservice.domain.user.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record CurrentUserResult(
        UUID id,
        String email,
        String displayName,
        String marketCountry,
        UserStatus status,
        CurrentSettlementAccountResult settlementAccount,
        Instant createdAt,
        Instant updatedAt
) {
}
