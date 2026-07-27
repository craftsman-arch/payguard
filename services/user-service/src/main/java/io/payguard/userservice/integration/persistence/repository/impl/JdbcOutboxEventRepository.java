package io.payguard.userservice.integration.persistence.repository.impl;

import io.payguard.userservice.application.event.OutboxEvent;
import io.payguard.userservice.application.event.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcOutboxEventRepository implements OutboxEventRepository {

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
                :nextAttemptAt
            )
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
                        )
                        .addValue(
                                "nextAttemptAt",
                                event.nextAttemptAt()
                        );

        jdbcTemplate.update(
                INSERT_EVENT,
                parameters
        );
    }
}
