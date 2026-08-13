package io.payguard.userservice.integration.web.user.response;

import io.payguard.userservice.domain.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Current PayGuard user profile.")
public record CurrentUserResponse(
        UUID id,
        String email,
        String displayName,
        String marketCountry,
        UserStatus status,
        CurrentSettlementAccountResponse settlementAccount,
        Instant createdAt,
        Instant updatedAt
) {
}
