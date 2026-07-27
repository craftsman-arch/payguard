CREATE TABLE outbox_events
(
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(64)   NOT NULL,
    aggregate_id    VARCHAR(255)  NOT NULL,
    event_type      VARCHAR(128)  NOT NULL,
    event_body      JSONB         NOT NULL,
    occurred_at     TIMESTAMPTZ   NOT NULL,
    correlation_id  VARCHAR(255),
    published_at    TIMESTAMPTZ,
    attempts        INTEGER       NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ   NOT NULL,
    last_error      VARCHAR(1000),

    CONSTRAINT chk_outbox_events_attempts
        CHECK (attempts >= 0)
);

CREATE INDEX idx_outbox_events_pending
    ON outbox_events (next_attempt_at, occurred_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (
        aggregate_type,
        aggregate_id,
        occurred_at
    );
