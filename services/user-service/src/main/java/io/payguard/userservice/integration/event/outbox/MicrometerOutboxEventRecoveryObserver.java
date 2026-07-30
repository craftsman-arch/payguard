package io.payguard.userservice.integration.event.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.payguard.userservice.application.event.recovery.OutboxEventRecoveryObserver;
import io.payguard.userservice.application.event.recovery.RecoverOutboxEventResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "outbox.observability",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MicrometerOutboxEventRecoveryObserver implements OutboxEventRecoveryObserver {

    private final Counter recoveredEvents;

    public MicrometerOutboxEventRecoveryObserver(MeterRegistry meterRegistry) {

        this.recoveredEvents = Counter
                .builder("outbox.recovery.completed")
                .description(
                        "Number of exhausted outbox events returned to the publication pipeline."
                )
                .register(meterRegistry);
    }

    @Override
    public void recovered(RecoverOutboxEventResult result) {

        recoveredEvents.increment();
    }
}
