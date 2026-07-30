CREATE INDEX idx_outbox_events_published_retention
    ON outbox_events (
        published_at,
        sequence_number
    )
    WHERE published_at IS NOT NULL;
