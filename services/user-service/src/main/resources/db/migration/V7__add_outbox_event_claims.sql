ALTER TABLE outbox_events
    ADD COLUMN claim_id UUID,
    ADD COLUMN claimed_until TIMESTAMPTZ,
    ADD COLUMN failed_at TIMESTAMPTZ,
    ADD COLUMN sequence_number BIGINT
        GENERATED ALWAYS AS IDENTITY,
    ADD COLUMN created_at TIMESTAMPTZ
        NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE outbox_events
    ADD CONSTRAINT chk_outbox_events_claim
        CHECK (
            (
                claim_id IS NULL
                AND claimed_until IS NULL
            )
            OR
            (
                claim_id IS NOT NULL
                AND claimed_until IS NOT NULL
            )
        ),

    ADD CONSTRAINT uk_outbox_events_sequence_number
        UNIQUE (sequence_number);

DROP INDEX idx_outbox_events_pending;
DROP INDEX idx_outbox_events_aggregate;

CREATE INDEX idx_outbox_events_pending
    ON outbox_events (
        next_attempt_at,
        claimed_until,
        sequence_number
    )
    WHERE published_at IS NULL
      AND failed_at IS NULL;

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (
        aggregate_type,
        aggregate_id,
        sequence_number
    );
