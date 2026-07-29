package io.payguard.userservice.integration.web.event;

import lombok.NonNull;

import java.util.UUID;

public record RecoverOutboxEventResponse(

        @NonNull UUID recoveryId,
        @NonNull UUID eventId,
        @NonNull String status

) {
}
