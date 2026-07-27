ALTER TABLE merchants
    ADD COLUMN payment_account_status VARCHAR(32),
    ADD COLUMN payment_account_status_reason VARCHAR(255),
    ADD COLUMN last_payment_account_event_at TIMESTAMPTZ;

UPDATE merchants
SET payment_account_status = CASE
    WHEN status = 'ACTIVE' THEN 'ACTIVE'
    ELSE 'PENDING_ONBOARDING'
END;

ALTER TABLE merchants
    ALTER COLUMN payment_account_status SET NOT NULL,
    ADD CONSTRAINT chk_merchants_payment_account_status
        CHECK (
            payment_account_status IN (
                'PENDING_ONBOARDING',
                'ACTIVE',
                'RESTRICTED',
                'DISABLED'
            )
        );