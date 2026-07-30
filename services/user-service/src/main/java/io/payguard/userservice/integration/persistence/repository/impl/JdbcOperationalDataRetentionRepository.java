package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.application.housekeeping.OperationalDataRetentionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Repository
@RequiredArgsConstructor
public class JdbcOperationalDataRetentionRepository implements OperationalDataRetentionRepository {

    private static final String DELETE_PROCESSED_STRIPE_EVENTS = """
            WITH candidates AS (
                SELECT event_id
                FROM processed_stripe_events
                WHERE processed_at < :cutoff
                ORDER BY processed_at
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            DELETE FROM processed_stripe_events event
            USING candidates
            WHERE event.event_id = candidates.event_id
            """;

    private static final String DELETE_PUBLISHED_OUTBOX_EVENTS = """
            WITH candidates AS (
                SELECT id
                FROM outbox_events
                WHERE published_at IS NOT NULL
                  AND published_at < :cutoff
                ORDER BY
                    published_at,
                    sequence_number
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            DELETE FROM outbox_events event
            USING candidates
            WHERE event.id = candidates.id
            """;

    private static final String DELETE_OUTBOX_EVENT_RECOVERIES = """
            WITH candidates AS (
                SELECT id
                FROM outbox_event_recoveries
                WHERE recovered_at < :cutoff
                ORDER BY
                    recovered_at,
                    id
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            DELETE FROM outbox_event_recoveries recovery
            USING candidates
            WHERE recovery.id = candidates.id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteProcessedStripeEvents(@NonNull Instant processedBefore, int batchSize) {

        return deleteBatch(
                DELETE_PROCESSED_STRIPE_EVENTS,
                processedBefore,
                batchSize
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deletePublishedOutboxEvents(@NonNull Instant publishedBefore, int batchSize) {

        return deleteBatch(
                DELETE_PUBLISHED_OUTBOX_EVENTS,
                publishedBefore,
                batchSize
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteOutboxEventRecoveries(@NonNull Instant recoveredBefore, int batchSize) {

        return deleteBatch(
                DELETE_OUTBOX_EVENT_RECOVERIES,
                recoveredBefore,
                batchSize
        );
    }

    private int deleteBatch(String statement, Instant cutoff, int batchSize) {

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "Operational retention batch size must be positive."
            );
        }

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "cutoff",
                                OffsetDateTime.ofInstant(
                                        cutoff,
                                        ZoneOffset.UTC
                                )
                        )
                        .addValue(
                                "batchSize",
                                batchSize
                        );

        return jdbcTemplate.update(
                statement,
                parameters
        );
    }
}
