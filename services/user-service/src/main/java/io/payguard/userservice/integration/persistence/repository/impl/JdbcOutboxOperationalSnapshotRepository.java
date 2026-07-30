package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.application.event.observability.OutboxOperationalSnapshot;
import io.payguard.userservice.application.event.observability.OutboxOperationalSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcOutboxOperationalSnapshotRepository
        implements OutboxOperationalSnapshotRepository {

    private static final String LOAD_SNAPSHOT = """
            SELECT
                COUNT(*) FILTER (
                    WHERE published_at IS NULL
                      AND failed_at IS NULL
                ) AS pending_events,
                COUNT(*) FILTER (
                    WHERE published_at IS NULL
                      AND failed_at IS NOT NULL
                ) AS exhausted_events,
                COUNT(
                    DISTINCT (
                        aggregate_type,
                        aggregate_id
                    )
                ) FILTER (
                    WHERE published_at IS NULL
                      AND failed_at IS NOT NULL
                ) AS blocked_aggregates,
                COALESCE(
                    GREATEST(
                        EXTRACT(
                            EPOCH FROM (
                                CURRENT_TIMESTAMP
                                - MIN(created_at) FILTER (
                                    WHERE published_at IS NULL
                                      AND failed_at IS NULL
                                )
                            )
                        ),
                        0
                    ),
                    0
                )::BIGINT AS oldest_pending_event_age_seconds,
                COALESCE(
                    GREATEST(
                        EXTRACT(
                            EPOCH FROM (
                                CURRENT_TIMESTAMP
                                - MIN(created_at) FILTER (
                                    WHERE published_at IS NULL
                                      AND failed_at IS NOT NULL
                                )
                            )
                        ),
                        0
                    ),
                    0
                )::BIGINT AS oldest_exhausted_event_age_seconds
            FROM outbox_events
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public OutboxOperationalSnapshot load() {

        return jdbcTemplate.queryForObject(
                LOAD_SNAPSHOT,
                EmptySqlParameterSource.INSTANCE,
                (resultSet, rowNumber) ->
                        new OutboxOperationalSnapshot(
                                resultSet.getLong(
                                        "pending_events"
                                ),
                                resultSet.getLong(
                                        "exhausted_events"
                                ),
                                resultSet.getLong(
                                        "blocked_aggregates"
                                ),
                                resultSet.getLong(
                                        "oldest_pending_event_age_seconds"
                                ),
                                resultSet.getLong(
                                        "oldest_exhausted_event_age_seconds"
                                )
                        )
        );
    }
}
