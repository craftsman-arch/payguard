ALTER TABLE merchants
    ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;

ALTER TABLE merchants
    ADD CONSTRAINT chk_merchants_revision
        CHECK (revision >= 0);
