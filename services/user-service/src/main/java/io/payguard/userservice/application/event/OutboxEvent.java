package io.payguard.userservice.application.event;

import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

public record OutboxEvent(

        @NonNull UUID id,
        @NonNull String aggregateType,
        @NonNull String aggregateId,
        @NonNull String eventType,
        @NonNull String eventBody,
        @NonNull Instant occurredAt,
        String correlationId,
        @NonNull Instant nextAttemptAt

) {

    public OutboxEvent {

        requireText(
                aggregateType,
                "Outbox aggregate type must not be blank."
        );

        requireText(
                aggregateId,
                "Outbox aggregate id must not be blank."
        );

        requireText(
                eventType,
                "Outbox event type must not be blank."
        );

        requireText(
                eventBody,
                "Outbox event body must not be blank."
        );
    }
}
