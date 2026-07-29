package io.payguard.userservice.application.event.recovery;

import java.util.UUID;

public class OutboxEventNotExhaustedException extends RuntimeException {

    public OutboxEventNotExhaustedException(UUID eventId) {
        super(
                "Outbox event [%s] has not exhausted publication attempts."
                        .formatted(eventId)
        );
    }
}
