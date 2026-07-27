package io.payguard.userservice.application.event;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface OutboxEventPublicationRepository {

    List<ClaimedOutboxEvent> claimBatch(
            UUID claimId,
            Duration leaseDuration,
            int batchSize
    );

    boolean markPublished(
            UUID eventId,
            UUID claimId
    );

    boolean reschedule(
            UUID eventId,
            UUID claimId,
            Duration retryBackoff,
            String lastError
    );

    boolean markFailed(
            UUID eventId,
            UUID claimId,
            String lastError
    );
}
