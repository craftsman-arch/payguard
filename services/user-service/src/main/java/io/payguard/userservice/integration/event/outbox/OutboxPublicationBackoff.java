package io.payguard.userservice.integration.event.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OutboxPublicationBackoff {

    private final OutboxPublisherProperties properties;

    public Duration forAttempt(int attempt) {

        if (attempt <= 0) {
            throw new IllegalArgumentException(
                    "Outbox publication attempt must be positive."
            );
        }

        long delay = properties.baseBackoff().toMillis();
        long maximumDelay = properties.maxBackoff().toMillis();

        for (int index = 1; index < attempt; index++) {

            if (delay >= maximumDelay
                    || delay > maximumDelay / 2) {

                return properties.maxBackoff();
            }

            delay *= 2;
        }

        return Duration.ofMillis(
                Math.min(delay, maximumDelay)
        );
    }
}
