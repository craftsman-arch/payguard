package io.payguard.userservice.application.event.recovery;

import java.util.UUID;

public class OutboxEventNotFoundException extends RuntimeException {

    public OutboxEventNotFoundException(UUID eventId) {
        super(
                "Outbox event [%s] was not found."
                        .formatted(eventId)
        );
    }
}
