package io.payguard.userservice.integration.event.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.payguard.userservice.application.event.observability.OutboxOperationalSnapshot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty(
        prefix = "outbox.observability",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxOperationalMetrics {

    private final AtomicReference<OutboxOperationalSnapshot> snapshot = new AtomicReference<>(OutboxOperationalSnapshot.empty());
    private final Counter refreshErrors;

    public OutboxOperationalMetrics(MeterRegistry meterRegistry) {

        this.refreshErrors = Counter
                .builder("outbox.observability.refresh.errors")
                .description(
                        "Number of failed outbox operational metric refreshes."
                )
                .register(meterRegistry);

        registerGauge(
                meterRegistry,
                "outbox.events.pending",
                "Number of unpublished outbox events that are not exhausted.",
                OutboxOperationalSnapshot::pendingEvents
        );

        registerGauge(
                meterRegistry,
                "outbox.events.exhausted",
                "Number of exhausted unpublished outbox events.",
                OutboxOperationalSnapshot::exhaustedEvents
        );

        registerGauge(
                meterRegistry,
                "outbox.aggregates.blocked",
                "Number of aggregates blocked by an exhausted outbox event.",
                OutboxOperationalSnapshot::blockedAggregates
        );

        registerAgeGauge(
                meterRegistry,
                "outbox.event.oldest.pending.age",
                "Age of the oldest pending outbox event in seconds.",
                OutboxOperationalSnapshot::oldestPendingEventAgeSeconds
        );

        registerAgeGauge(
                meterRegistry,
                "outbox.event.oldest.exhausted.age",
                "Age of the oldest exhausted outbox event in seconds.",
                OutboxOperationalSnapshot::oldestExhaustedEventAgeSeconds
        );
    }

    void update(OutboxOperationalSnapshot refreshed) {
        snapshot.set(refreshed);
    }

    void recordRefreshError() {
        refreshErrors.increment();
    }

    private void registerGauge(
            MeterRegistry meterRegistry,
            String name,
            String description,
            SnapshotValue value
    ) {

        Gauge.builder(
                        name,
                        snapshot,
                        current -> value.read(current.get())
                )
                .description(description)
                .register(meterRegistry);
    }

    private void registerAgeGauge(
            MeterRegistry meterRegistry,
            String name,
            String description,
            SnapshotValue value
    ) {

        Gauge.builder(
                        name,
                        snapshot,
                        current -> value.read(current.get())
                )
                .description(description)
                .baseUnit("seconds")
                .register(meterRegistry);
    }

    @FunctionalInterface
    private interface SnapshotValue {

        double read(OutboxOperationalSnapshot snapshot);
    }
}
