package io.payguard.userservice.integration.event.contracts;

import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

public record EventEnvelope<T>(

        @NonNull UUID id,
        @NonNull String type,
        @NonNull String source,
        @NonNull Instant time,
        @NonNull String subject,
        String correlationId,
        @NonNull T data

) {

    public EventEnvelope {

        requireText(type, "Event type must not be blank.");
        requireText(source, "Event source must not be blank.");
        requireText(subject, "Event subject must not be blank.");
    }
}
