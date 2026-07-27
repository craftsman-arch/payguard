package io.payguard.userservice.application.event;

import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

public record ClaimedOutboxEvent(

        @NonNull UUID id,
        @NonNull UUID claimId,
        long sequenceNumber,
        @NonNull String aggregateType,
        @NonNull String aggregateId,
        @NonNull String eventType,
        @NonNull String eventBody,
        @NonNull Instant occurredAt,
        String correlationId,
        int attempts

) {

    public ClaimedOutboxEvent {

        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Outbox sequence number must be positive."
            );
        }

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

        if (attempts < 0) {
            throw new IllegalArgumentException(
                    "Outbox publication attempts must not be negative."
            );
        }
    }
}
