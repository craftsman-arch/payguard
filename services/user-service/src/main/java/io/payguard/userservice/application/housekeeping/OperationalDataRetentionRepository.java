package io.payguard.userservice.application.housekeeping;

import java.time.Instant;

public interface OperationalDataRetentionRepository {

    int deleteProcessedStripeEvents(Instant processedBefore, int batchSize);

    int deletePublishedOutboxEvents(Instant publishedBefore, int batchSize);

    int deleteOutboxEventRecoveries(Instant recoveredBefore, int batchSize);
}
