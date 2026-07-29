package io.payguard.userservice.application.event.recovery;

import java.util.UUID;

public class OutboxEventAlreadyPublishedException extends RuntimeException {

    public OutboxEventAlreadyPublishedException(UUID eventId) {
        super(
                "Outbox event [%s] has already been published."
                        .formatted(eventId)
        );
    }
}
