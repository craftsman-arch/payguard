package io.payguard.userservice.application.event.recovery;

import lombok.NonNull;

import java.util.UUID;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

public record RecoverOutboxEventCommand(

        @NonNull UUID eventId,
        @NonNull String recoveredBy,
        @NonNull String reason

) {

    public RecoverOutboxEventCommand {

        requireText(
                recoveredBy,
                "Outbox event recovery actor must not be blank."
        );

        requireText(
                reason,
                "Outbox event recovery reason must not be blank."
        );
    }
}
