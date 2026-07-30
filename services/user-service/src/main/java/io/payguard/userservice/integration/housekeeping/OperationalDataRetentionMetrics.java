package io.payguard.userservice.integration.housekeeping;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntSupplier;

@Component
@ConditionalOnProperty(
        prefix = "housekeeping.retention",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OperationalDataRetentionMetrics {

    private static final String STORAGE_TAG = "storage";

    private final MeterRegistry meterRegistry;
    private final Map<OperationalDataRetentionTarget, Counter> executions;
    private final Map<OperationalDataRetentionTarget, Counter> deletedRows;
    private final Map<OperationalDataRetentionTarget, Counter> failures;
    private final Map<OperationalDataRetentionTarget, Timer> durations;

    public OperationalDataRetentionMetrics(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;
        this.executions = new EnumMap<>(OperationalDataRetentionTarget.class);
        this.deletedRows = new EnumMap<>(OperationalDataRetentionTarget.class);
        this.failures = new EnumMap<>(OperationalDataRetentionTarget.class);
        this.durations = new EnumMap<>(OperationalDataRetentionTarget.class);

        for (OperationalDataRetentionTarget target : OperationalDataRetentionTarget.values()) {

            executions.put(
                    target,
                    counter(
                            "housekeeping.retention.executions",
                            "Number of operational retention cleanup executions.",
                            target
                    )
            );

            deletedRows.put(
                    target,
                    counter(
                            "housekeeping.retention.deleted",
                            "Number of operational records deleted by retention cleanup.",
                            target
                    )
            );

            failures.put(
                    target,
                    counter(
                            "housekeeping.retention.failures",
                            "Number of failed operational retention cleanup executions.",
                            target
                    )
            );

            durations.put(
                    target,
                    Timer.builder(
                                    "housekeeping.retention.duration"
                            )
                            .description(
                                    "Operational retention cleanup duration."
                            )
                            .tag(
                                    STORAGE_TAG,
                                    target.metricTag()
                            )
                            .register(meterRegistry)
            );
        }
    }

    int observe(OperationalDataRetentionTarget target, IntSupplier cleanup) {

        executions.get(target).increment();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {

            int deleted = cleanup.getAsInt();

            if (deleted > 0) {
                deletedRows.get(target).increment(deleted);
            }

            return deleted;

        } catch (RuntimeException exception) {

            failures.get(target).increment();
            throw exception;

        } finally {

            sample.stop(durations.get(target));
        }
    }

    private Counter counter(String name, String description, OperationalDataRetentionTarget target) {

        return Counter
                .builder(name)
                .description(description)
                .tag(
                        STORAGE_TAG,
                        target.metricTag()
                )
                .register(meterRegistry);
    }
}
