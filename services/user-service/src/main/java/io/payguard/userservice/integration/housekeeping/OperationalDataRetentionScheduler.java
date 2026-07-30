package io.payguard.userservice.integration.housekeeping;

import io.payguard.userservice.application.housekeeping.OperationalDataRetentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.function.IntSupplier;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "housekeeping.retention",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OperationalDataRetentionScheduler {

    private final OperationalDataRetentionService retentionService;
    private final OperationalDataRetentionMetrics metrics;

    @Scheduled(
            cron = "${housekeeping.retention.cron:0 0 3 * * *}",
            zone = "${housekeeping.retention.zone:UTC}"
    )
    public void cleanup() {

        cleanup(
                OperationalDataRetentionTarget.PROCESSED_STRIPE_EVENTS,
                retentionService::cleanupProcessedStripeEvents
        );

        cleanup(
                OperationalDataRetentionTarget.PUBLISHED_OUTBOX_EVENTS,
                retentionService::cleanupPublishedOutboxEvents
        );

        cleanup(
                OperationalDataRetentionTarget.OUTBOX_EVENT_RECOVERIES,
                retentionService::cleanupOutboxEventRecoveries
        );
    }

    private void cleanup(OperationalDataRetentionTarget target, IntSupplier operation) {

        try {

            int deleted = metrics.observe(target, operation);

            log.info(
                    "Completed operational retention cleanup [{}]; deleted [{}] records.",
                    target.metricTag(),
                    deleted
            );

        } catch (Exception exception) {

            log.error(
                    "Operational retention cleanup [{}] failed.",
                    target.metricTag(),
                    exception
            );
        }
    }
}
