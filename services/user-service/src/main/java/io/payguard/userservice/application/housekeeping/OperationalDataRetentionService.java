package io.payguard.userservice.application.housekeeping;

import io.payguard.userservice.application.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OperationalDataRetentionService {

    private final OperationalDataRetentionRepository retentionRepository;
    private final OperationalDataRetentionProperties properties;
    private final TimeProvider timeProvider;

    public int cleanupProcessedStripeEvents() {

        return retentionRepository.deleteProcessedStripeEvents(
                cutoff(properties.processedStripeEvents()),
                properties.batchSize()
        );
    }

    public int cleanupPublishedOutboxEvents() {

        return retentionRepository.deletePublishedOutboxEvents(
                cutoff(properties.publishedOutboxEvents()),
                properties.batchSize()
        );
    }

    public int cleanupOutboxEventRecoveries() {

        return retentionRepository.deleteOutboxEventRecoveries(
                cutoff(properties.outboxEventRecoveries()),
                properties.batchSize()
        );
    }

    private Instant cutoff(Duration retention) {

        return timeProvider.now().minus(retention);
    }
}
