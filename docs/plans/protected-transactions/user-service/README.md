# User Service evolution

## Goal

Generalize the marketplace-only Merchant model into a platform user with an
optional provider-backed settlement account, while preserving the existing
identity, Stripe Connect, webhook, outbox and operational reliability work.

## 01 - Record the model change

- Add an ADR explaining the protected-transaction product model.
- Define `PayGuardUser` and optional `SettlementAccount` boundaries.
- Define `USER`, `ADMIN` and `RISK_ANALYST` as identity roles.
- Explicitly reject permanent `BUYER` and `SELLER` realm roles.
- Document migration and compatibility decisions before changing APIs.

## 02 - Introduce the platform user

- Replace Merchant identity/profile concepts with `PayGuardUser` and `UserId`.
- Generalize self-registration, verification-email resend and current-user
  profile endpoints.
- Replace the `MERCHANT` Keycloak role with `USER`.
- Preserve password policy, email verification, OTP and PKCE login.
- Preserve staged registration and idempotent profile creation.
- Add forward-only database migrations for existing merchant data.

## 03 - Extract the settlement account

- Move provider account ID, onboarding lifecycle, requirements, capabilities
  and provider event time out of the User aggregate.
- Introduce an optional one-to-one `SettlementAccount` aggregate or explicitly
  owned entity with independent lifecycle invariants.
- A user may exist and fund deals without a settlement account.
- Opening a settlement account starts Stripe Connect onboarding.
- Preserve signed webhook verification, durable deduplication, stale-event
  handling and provider-neutral normalization.
- Preserve optimistic concurrency and atomic outbox persistence.

## 04 - Generalize internal eligibility

- Add a protected participation-context endpoint for active-user eligibility.
- Add a protected settlement-context endpoint for payee eligibility.
- Express supported operations explicitly, including `RECEIVE_TRANSFER`.
- Do not expose raw Stripe requirements to downstream services.
- Do not let callers choose provider account IDs or bypass eligibility policy.

## 05 - Migrate contracts and operations

- Replace `merchant.payment-account-status-changed.v1` with a versioned
  settlement-account lifecycle event.
- Keep legacy contracts documented as superseded until all consumers migrate.
- Update OpenAPI, Gateway routes, Keycloak realm import and local test commands.
- Rename metrics only when the old name is materially misleading; document any
  compatibility impact.
- Update architecture documents after each behavior is implemented.

## Preserved implementation

- Keycloak Admin integration and resilience;
- Stripe Connect adapter;
- webhook signature verification and durable deduplication;
- transactional outbox, retries, poisoned ordering and recovery;
- operational metrics and retention;
- API error sanitization and correlation IDs.

## Excluded

- deal lifecycle;
- payment collection;
- settlement execution and refunds;
- chat, evidence storage and dispute decisions.

