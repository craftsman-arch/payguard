package io.payguard.userservice.application.identity;

import io.payguard.userservice.domain.common.value.Email;
import lombok.NonNull;

public record AuthenticatedUser(

        @NonNull String id,
        @NonNull Email email,
        boolean emailVerified

) {
}
