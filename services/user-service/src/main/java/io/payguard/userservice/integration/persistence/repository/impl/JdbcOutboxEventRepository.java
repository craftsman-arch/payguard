package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.application.event.ClaimedOutboxEvent;
import io.payguard.userservice.application.event.OutboxEvent;
import io.payguard.userservice.application.event.OutboxEventPublicationRepository;
import io.payguard.userservice.application.event.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcOutboxEventRepository
        implements OutboxEventRepository,
        OutboxEventPublicationRepository {

    private static final String INSERT_EVENT = """
            INSERT INTO outbox_events (
                id,
                aggregate_type,
                aggregate_id,
                event_type,
                event_body,
                occurred_at,
                correlation_id,
                next_attempt_at
            )
            VALUES (
                :id,
                :aggregateType,
                :aggregateId,
                :eventType,
                CAST(:eventBody AS JSONB),
                :occurredAt,
                :correlationId,
                CURRENT_TIMESTAMP
            )
            """;

    private static final String CLAIM_BATCH = """
            WITH candidates AS (
                SELECT
                    event.id,
                    event.sequence_number
                FROM outbox_events event
                WHERE event.published_at IS NULL
                  AND event.failed_at IS NULL
                  AND event.next_attempt_at
                      <= CURRENT_TIMESTAMP
                  AND (
                      event.claim_id IS NULL
                      OR event.claimed_until
                          <= CURRENT_TIMESTAMP
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM outbox_events earlier
                      WHERE earlier.aggregate_type
                                = event.aggregate_type
                        AND earlier.aggregate_id
                                = event.aggregate_id
                        AND earlier.published_at IS NULL
                        AND earlier.sequence_number
                                < event.sequence_number
                  )
                ORDER BY event.sequence_number
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            UPDATE outbox_events event
            SET claim_id = :claimId,
                claimed_until =
                    CURRENT_TIMESTAMP
                    + (
                        :leaseDurationMillis
                        * INTERVAL '1 millisecond'
                    )
            FROM candidates
            WHERE event.id = candidates.id
            RETURNING
                event.id,
                event.claim_id,
                event.sequence_number,
                event.aggregate_type,
                event.aggregate_id,
                event.event_type,
                event.event_body::text AS event_body,
                event.occurred_at,
                event.correlation_id,
                event.attempts
            """;

    private static final String MARK_PUBLISHED = """
            UPDATE outbox_events
            SET published_at = CURRENT_TIMESTAMP,
                claim_id = NULL,
                claimed_until = NULL,
                last_error = NULL
            WHERE id = :eventId
              AND claim_id = :claimId
              AND published_at IS NULL
              AND failed_at IS NULL
            """;

    private static final String RESCHEDULE = """
            UPDATE outbox_events
            SET attempts = attempts + 1,
                next_attempt_at =
                    CURRENT_TIMESTAMP
                    + (
                        :retryBackoffMillis
                        * INTERVAL '1 millisecond'
                    ),
                last_error = :lastError,
                claim_id = NULL,
                claimed_until = NULL
            WHERE id = :eventId
              AND claim_id = :claimId
              AND published_at IS NULL
              AND failed_at IS NULL
            """;

    private static final String MARK_FAILED = """
            UPDATE outbox_events
            SET attempts = attempts + 1,
                failed_at = CURRENT_TIMESTAMP,
                last_error = :lastError,
                claim_id = NULL,
                claimed_until = NULL
            WHERE id = :eventId
              AND claim_id = :claimId
              AND published_at IS NULL
              AND failed_at IS NULL
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void add(OutboxEvent event) {

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("id", event.id())
                        .addValue(
                                "aggregateType",
                                event.aggregateType()
                        )
                        .addValue(
                                "aggregateId",
                                event.aggregateId()
                        )
                        .addValue(
                                "eventType",
                                event.eventType()
                        )
                        .addValue(
                                "eventBody",
                                event.eventBody()
                        )
                        .addValue(
                                "occurredAt",
                                event.occurredAt()
                        )
                        .addValue(
                                "correlationId",
                                event.correlationId()
                        );

        jdbcTemplate.update(
                INSERT_EVENT,
                parameters
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<ClaimedOutboxEvent> claimBatch(
            UUID claimId,
            Duration leaseDuration,
            int batchSize
    ) {

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "Outbox claim batch size must be positive."
            );
        }

        if (leaseDuration.isZero()
                || leaseDuration.isNegative()) {

            throw new IllegalArgumentException(
                    "Outbox claim lease duration must be positive."
            );
        }

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("claimId", claimId)
                        .addValue(
                                "leaseDurationMillis",
                                leaseDuration.toMillis()
                        )
                        .addValue("batchSize", batchSize);

        return jdbcTemplate
                .query(
                        CLAIM_BATCH,
                        parameters,
                        this::mapClaimedEvent
                )
                .stream()
                .sorted(
                        Comparator.comparingLong(
                                ClaimedOutboxEvent::sequenceNumber
                        )
                )
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markPublished(
            UUID eventId,
            UUID claimId
    ) {

        return jdbcTemplate.update(
                MARK_PUBLISHED,
                ownershipParameters(
                        eventId,
                        claimId
                )
        ) == 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean reschedule(
            UUID eventId,
            UUID claimId,
            Duration retryBackoff,
            String lastError
    ) {

        if (retryBackoff.isZero()
                || retryBackoff.isNegative()) {

            throw new IllegalArgumentException(
                    "Outbox retry backoff must be positive."
            );
        }

        MapSqlParameterSource parameters =
                ownershipParameters(
                        eventId,
                        claimId
                )
                        .addValue(
                                "retryBackoffMillis",
                                retryBackoff.toMillis()
                        )
                        .addValue(
                                "lastError",
                                lastError
                        );

        return jdbcTemplate.update(
                RESCHEDULE,
                parameters
        ) == 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markFailed(
            UUID eventId,
            UUID claimId,
            String lastError
    ) {

        MapSqlParameterSource parameters =
                ownershipParameters(
                        eventId,
                        claimId
                )
                        .addValue(
                                "lastError",
                                lastError
                        );

        return jdbcTemplate.update(
                MARK_FAILED,
                parameters
        ) == 1;
    }

    private ClaimedOutboxEvent mapClaimedEvent(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {

        return new ClaimedOutboxEvent(
                resultSet.getObject(
                        "id",
                        UUID.class
                ),
                resultSet.getObject(
                        "claim_id",
                        UUID.class
                ),
                resultSet.getLong(
                        "sequence_number"
                ),
                resultSet.getString(
                        "aggregate_type"
                ),
                resultSet.getString(
                        "aggregate_id"
                ),
                resultSet.getString(
                        "event_type"
                ),
                resultSet.getString(
                        "event_body"
                ),
                resultSet.getObject(
                        "occurred_at",
                        OffsetDateTime.class
                ).toInstant(),
                resultSet.getString(
                        "correlation_id"
                ),
                resultSet.getInt(
                        "attempts"
                )
        );
    }

    private MapSqlParameterSource ownershipParameters(
            UUID eventId,
            UUID claimId
    ) {

        return new MapSqlParameterSource()
                .addValue("eventId", eventId)
                .addValue("claimId", claimId);
    }
}
