package io.payguard.userservice.application.event.observability;

public record OutboxOperationalSnapshot(

        long pendingEvents,
        long exhaustedEvents,
        long blockedAggregates,
        long oldestPendingEventAgeSeconds,
        long oldestExhaustedEventAgeSeconds

) {

    public static OutboxOperationalSnapshot empty() {

        return new OutboxOperationalSnapshot(0, 0, 0, 0, 0);
    }

    public OutboxOperationalSnapshot {

        requireNonNegative(
                pendingEvents,
                "Pending outbox event count must not be negative."
        );

        requireNonNegative(
                exhaustedEvents,
                "Exhausted outbox event count must not be negative."
        );

        requireNonNegative(
                blockedAggregates,
                "Blocked outbox aggregate count must not be negative."
        );

        requireNonNegative(
                oldestPendingEventAgeSeconds,
                "Oldest pending outbox event age must not be negative."
        );

        requireNonNegative(
                oldestExhaustedEventAgeSeconds,
                "Oldest exhausted outbox event age must not be negative."
        );
    }

    private static void requireNonNegative(long value, String message) {

        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
