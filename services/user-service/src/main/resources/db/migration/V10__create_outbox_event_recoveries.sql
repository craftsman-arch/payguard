CREATE TABLE outbox_event_recoveries
(
    id                  UUID PRIMARY KEY,
    event_id            UUID          NOT NULL,
    recovered_at        TIMESTAMPTZ   NOT NULL
                        DEFAULT CURRENT_TIMESTAMP,
    recovered_by        VARCHAR(255)  NOT NULL,
    recovery_reason     VARCHAR(1000) NOT NULL,
    previous_attempts   INTEGER       NOT NULL,
    previous_failed_at  TIMESTAMPTZ   NOT NULL,
    previous_last_error VARCHAR(1000),

    CONSTRAINT chk_outbox_event_recoveries_attempts
        CHECK (previous_attempts > 0),

    CONSTRAINT chk_outbox_event_recoveries_recovered_by
        CHECK (BTRIM(recovered_by) <> ''),

    CONSTRAINT chk_outbox_event_recoveries_reason
        CHECK (BTRIM(recovery_reason) <> '')
);

CREATE INDEX idx_outbox_event_recoveries_event
    ON outbox_event_recoveries (
        event_id,
        recovered_at DESC
    );
