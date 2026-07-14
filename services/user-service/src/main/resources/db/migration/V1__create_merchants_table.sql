CREATE TABLE merchants
(
    id                  UUID PRIMARY KEY,

    email               VARCHAR(255) NOT NULL,
    legal_name          VARCHAR(255) NOT NULL,
    business_type       VARCHAR(32)  NOT NULL,
    country             VARCHAR(2) NOT NULL,

    identity_user_id    VARCHAR(255),
    payment_account_id  VARCHAR(255),

    status              VARCHAR(32)  NOT NULL,

    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uk_merchants_email
        UNIQUE (email),

    CONSTRAINT uk_merchants_identity_user_id
        UNIQUE (identity_user_id),

    CONSTRAINT uk_merchants_payment_account_id
        UNIQUE (payment_account_id),

    CONSTRAINT chk_merchants_business_type
        CHECK (
            business_type IN (
                'INDIVIDUAL',
                'COMPANY'
            )
        ),

    CONSTRAINT chk_merchants_status
        CHECK (
            status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED'
            )
        )
);

CREATE INDEX idx_merchants_status
    ON merchants (status);

CREATE INDEX idx_merchants_created_at
    ON merchants (created_at);