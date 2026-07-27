ALTER TABLE merchants
    ADD COLUMN card_payments_capability_active BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN transfers_capability_active BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE merchants
    ALTER COLUMN card_payments_capability_active DROP DEFAULT,
    ALTER COLUMN transfers_capability_active DROP DEFAULT;
