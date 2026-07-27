CREATE TABLE processed_stripe_events
(
    event_id      VARCHAR(255) PRIMARY KEY,
    event_type    VARCHAR(64)  NOT NULL,
    processed_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_processed_stripe_events_processed_at
    ON processed_stripe_events (processed_at);