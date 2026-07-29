package io.payguard.userservice.application.event.recovery;

import lombok.NonNull;

import java.util.UUID;

public record RecoverOutboxEventResult(

        @NonNull UUID recoveryId,
        @NonNull UUID eventId

) {
}
