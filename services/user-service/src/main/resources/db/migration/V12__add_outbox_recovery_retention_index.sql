CREATE INDEX idx_outbox_event_recoveries_retention
    ON outbox_event_recoveries (
        recovered_at,
        id
    );
