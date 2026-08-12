CREATE TABLE users
(
    id               UUID PRIMARY KEY,
    identity_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    display_name     VARCHAR(255) NOT NULL,
    market_country   VARCHAR(2)   NOT NULL,
    status           VARCHAR(32)  NOT NULL,
    revision         BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uk_users_identity_user_id
        UNIQUE (identity_user_id),

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED')),

    CONSTRAINT chk_users_revision
        CHECK (revision >= 0)
);

CREATE INDEX idx_users_status
    ON users (status);

CREATE INDEX idx_users_created_at
    ON users (created_at);

CREATE TABLE settlement_accounts
(
    id                          UUID PRIMARY KEY,
    user_id                     UUID         NOT NULL,
    provider                    VARCHAR(32)  NOT NULL,
    provider_account_id         VARCHAR(255),
    account_holder_name         VARCHAR(255) NOT NULL,
    account_holder_type         VARCHAR(32)  NOT NULL,
    country                     VARCHAR(2)   NOT NULL,
    creation_started_at         TIMESTAMPTZ,
    status                      VARCHAR(32)  NOT NULL,
    status_reason               VARCHAR(255),
    required_action             VARCHAR(64)  NOT NULL,
    last_provider_event_at      TIMESTAMPTZ,
    transfers_capability_active BOOLEAN      NOT NULL,
    revision                    BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMPTZ  NOT NULL,
    updated_at                  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_settlement_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT uk_settlement_accounts_user_id
        UNIQUE (user_id),

    CONSTRAINT uk_settlement_accounts_provider_account
        UNIQUE (provider, provider_account_id),

    CONSTRAINT chk_settlement_accounts_provider
        CHECK (provider IN ('STRIPE')),

    CONSTRAINT chk_settlement_accounts_holder_type
        CHECK (account_holder_type IN ('INDIVIDUAL', 'COMPANY')),

    CONSTRAINT chk_settlement_accounts_status
        CHECK (
            status IN (
                'PENDING_ONBOARDING',
                'ACTIVE',
                'RESTRICTED',
                'DISABLED'
            )
        ),

    CONSTRAINT chk_settlement_accounts_required_action
        CHECK (
            required_action IN (
                'CONTINUE_ONBOARDING',
                'WAIT_FOR_REVIEW',
                'CONTACT_SUPPORT',
                'NONE'
            )
        ),

    CONSTRAINT chk_settlement_accounts_revision
        CHECK (revision >= 0)
);

CREATE INDEX idx_settlement_accounts_status
    ON settlement_accounts (status);

CREATE INDEX idx_settlement_accounts_created_at
    ON settlement_accounts (created_at);
