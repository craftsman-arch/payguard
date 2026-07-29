# User Service application architecture

## Responsibility

User Service owns:

- merchant identity and lifecycle state;
- Keycloak identity linkage;
- Stripe Connect account linkage and normalized readiness state;
- the decision whether a merchant is ready for payments and eligible for
  destination charges;
- durable Stripe webhook deduplication;
- production of merchant lifecycle integration events through a transactional
  outbox.

Payment Service consumes the protected internal payment context and must not
trust a merchant or Stripe account ID supplied by an external caller.

## Component view

```mermaid
flowchart TB
    Client["Merchant portal / API client"]
    Gateway["API Gateway"]
    Stripe["Stripe Connect"]
    Keycloak["Keycloak"]
    Kafka["Kafka"]
    PaymentService["Payment Service"]

    subgraph UserService["User Service"]
        Web["Web adapters"]
        Application["Application use cases"]
        Domain["Merchant aggregate and policies"]
        Persistence["Persistence adapters"]
        ProviderAdapters["Keycloak / Stripe adapters"]
        OutboxPublisher["Outbox publisher"]
    end

    PostgreSQL[("PostgreSQL")]

    Client --> Gateway
    Gateway --> Web
    Stripe --> Gateway
    Web --> Application
    Application --> Domain
    Application --> Persistence
    Application --> ProviderAdapters
    Persistence --> PostgreSQL
    ProviderAdapters --> Keycloak
    ProviderAdapters --> Stripe
    OutboxPublisher --> PostgreSQL
    OutboxPublisher --> Kafka
    PaymentService --> Web
```

## Layers

### Domain

The domain contains the `Merchant` aggregate, value objects and payment-state
policies. It has no Spring, JPA, Stripe or Kafka dependencies.

The aggregate:

- normalizes provider state;
- resolves the required merchant action;
- calculates payment readiness and destination-charge eligibility;
- rejects stale provider snapshots;
- returns explicit update results and domain events.

`revision` is an optimistic-concurrency token carried through the detached
domain model. Business rules do not modify or depend on it.

### Application

The application layer coordinates commands, queries and transaction boundaries.
Important flows include:

- merchant registration and onboarding;
- current-merchant and internal payment-context queries;
- Stripe webhook verification, parsing, deduplication and dispatch;
- domain-event dispatch to the transactional outbox.

Webhook concurrency retries are orchestrated by a dedicated `RetryTemplate`.
Each attempt invokes a separate `REQUIRES_NEW` transactional processor.

### Integration

Integration adapters implement:

- JPA persistence for Merchant;
- JDBC persistence and claiming for webhook deduplication and outbox delivery;
- Keycloak Admin API operations;
- Stripe Connect API operations and webhook signature verification;
- Kafka publication;
- REST controllers and security mapping.

`MerchantEntity.revision` uses JPA `@Version`. Hibernate delegates the atomic
compare-and-set to PostgreSQL.

## Transaction boundaries

### Merchant registration

The local Merchant registration transaction commits before external onboarding
orchestration. Keycloak and Stripe resources are then provisioned, linked to the
Merchant and saved with optimistic concurrency.

The public React registration form supplies the merchant-selected password.
User Service forwards it directly to Keycloak Admin API as a permanent
credential; it does not store the password in the Merchant aggregate or any
local persistence model. Keycloak owns password-policy validation, password
hashing, email verification and OTP credentials.

New merchant identities are enabled with `emailVerified=false`, the `MERCHANT`
realm role and the `VERIFY_EMAIL` and `CONFIGURE_TOTP` required actions.
Registration does not issue authentication tokens. The merchant subsequently
authenticates through the public `merchant-portal` client using Authorization
Code with PKCE.

Provider failures are normalized at the web boundary: password-policy rejection
is `422`, identity conflict is `409`, provider unavailability is `503`, and an
unexpected identity-provider response is `502`. Original provider exceptions
remain available in server-side logs.

This flow currently uses best-effort compensation for external resources.
Durable onboarding reconciliation, partial-registration recovery and concurrent
registration control belong to the reliability scope.

### Stripe webhook

One local transaction contains:

```text
processed Stripe event claim
→ Merchant load and domain update
→ revision-checked Merchant persistence
→ outbox insert for each domain event
→ commit
```

Any failure rolls back all four effects. An optimistic conflict retries the
complete transaction after reloading Merchant state.

### Outbox publication

Kafka I/O is deliberately outside the database transaction:

```text
short transaction: claim bounded batch
→ Kafka send and acknowledgement
→ short transaction: mark published or reschedule
```

Publication is at least once. Consumers must deduplicate by event ID.

## Security boundaries

- Public merchant operations require an authenticated merchant token except for
  the registration flow defined by current API policy.
- Only `POST /api/webhooks/stripe` is public at API Gateway; User Service still
  requires a valid Stripe signature.
- Internal payment context requires a service token with the correct audience
  and payment-service scope.
- Browser SPA authentication uses the Keycloak authorization-code flow with
  PKCE.
- Merchant self-registration uses the PayGuard form and does not use invitation
  tokens or issue authentication tokens.
- Password credentials cross the browser, Gateway and User Service only on the
  registration request and terminate at Keycloak; they are not domain or event
  data.
