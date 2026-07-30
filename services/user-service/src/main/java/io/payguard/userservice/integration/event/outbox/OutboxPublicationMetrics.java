package io.payguard.userservice.integration.event.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "outbox.publisher",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxPublicationMetrics {

    private final Counter published;
    private final Counter confirmedFailures;
    private final Counter retries;
    private final Counter exhausted;
    private final Counter lostClaims;
    private final Counter staleClaimsRecovered;

    public OutboxPublicationMetrics(MeterRegistry meterRegistry) {

        this.published = counter(
                meterRegistry,
                "outbox.publication.published",
                "Number of successfully published outbox events."
        );

        this.confirmedFailures = counter(
                meterRegistry,
                "outbox.publication.attempt.failed",
                "Number of publication failures recorded in the outbox."
        );

        this.retries = counter(
                meterRegistry,
                "outbox.publication.retry",
                "Number of outbox events scheduled for another publication attempt."
        );

        this.exhausted = counter(
                meterRegistry,
                "outbox.publication.failed",
                "Number of outbox events that exhausted publication attempts."
        );

        this.lostClaims = counter(
                meterRegistry,
                "outbox.publication.lost.claim",
                "Number of publication outcomes rejected because claim ownership was lost."
        );

        this.staleClaimsRecovered = counter(
                meterRegistry,
                "outbox.publication.stale.claim.recovered",
                "Number of expired outbox claims recovered by a publisher."
        );
    }

    void recordPublished() {
        published.increment();
    }

    void recordConfirmedFailure() {
        confirmedFailures.increment();
    }

    void recordRetry() {
        retries.increment();
    }

    void recordExhausted() {
        exhausted.increment();
    }

    void recordLostClaim() {
        lostClaims.increment();
    }

    void recordStaleClaimRecovered() {
        staleClaimsRecovered.increment();
    }

    private Counter counter(
            MeterRegistry meterRegistry,
            String name,
            String description
    ) {

        return Counter
                .builder(name)
                .description(description)
                .register(meterRegistry);
    }
}
