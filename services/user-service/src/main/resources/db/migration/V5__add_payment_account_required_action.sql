ALTER TABLE merchants
    ADD COLUMN payment_account_required_action VARCHAR(64);

UPDATE merchants
SET payment_account_required_action = CASE
    WHEN payment_account_status = 'ACTIVE'
        THEN 'NONE'
    WHEN payment_account_status = 'DISABLED'
        THEN 'CONTACT_SUPPORT'
    ELSE 'CONTINUE_ONBOARDING'
END;

ALTER TABLE merchants
    ALTER COLUMN payment_account_required_action SET NOT NULL;

ALTER TABLE merchants
    ADD CONSTRAINT chk_merchants_payment_account_required_action
        CHECK (
            payment_account_required_action IN (
                'CONTINUE_ONBOARDING',
                'WAIT_FOR_REVIEW',
                'CONTACT_SUPPORT',
                'NONE'
            )
        );