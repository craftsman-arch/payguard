package io.payguard.userservice.integration.event.outbox;

import io.payguard.userservice.application.event.observability.OutboxOperationalSnapshot;
import io.payguard.userservice.application.event.observability.OutboxOperationalSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "outbox.observability",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxOperationalMetricsCollector {

    private final OutboxOperationalSnapshotRepository snapshotRepository;
    private final OutboxOperationalMetrics metrics;

    @Scheduled(
            fixedDelayString =
                    "${outbox.observability.refresh-interval:15s}",
            initialDelayString =
                    "${outbox.observability.initial-delay:0s}"
    )
    public void refresh() {

        try {

            OutboxOperationalSnapshot refreshed = snapshotRepository.load();
            metrics.update(refreshed);

            log.debug(
                    "Refreshed outbox operational metrics: pending [{}], exhausted [{}], blocked aggregates [{}].",
                    refreshed.pendingEvents(),
                    refreshed.exhaustedEvents(),
                    refreshed.blockedAggregates()
            );

        } catch (Exception exception) {

            metrics.recordRefreshError();

            log.error(
                    "Unable to refresh outbox operational metrics; retaining the last successful snapshot.",
                    exception
            );
        }
    }
}
