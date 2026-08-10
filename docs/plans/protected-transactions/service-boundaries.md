# Service boundaries and invariants

## Identity and participation

- Keycloak authenticates identities and supplies coarse platform authorities.
- `USER`, `ADMIN` and `RISK_ANALYST` are identity roles.
- `PAYER` and `PAYEE` are roles inside a particular deal.
- A user does not need a settlement account to fund a deal.
- A payee must have an eligible settlement account before a settlement can be
  released.

## Business ownership

User Service answers:

```text
Does this identity represent an active PayGuard user?
Can this user receive a settlement through the configured provider?
```

Deal Service answers:

```text
What did the parties agree to?
Has the deal been funded and delivered?
Is release authorized, canceled or disputed?
```

Payment Service answers:

```text
Were funds collected?
Were funds released to the payee?
Were funds refunded or reversed?
What provider operations prove those facts?
```

Fraud Engine recommends `ALLOW`, `REVIEW` or `BLOCK`. It never moves money and
does not own deal or payment state.

## Financial invariants

- Store money as integer minor units plus ISO 4217 currency.
- Never accept provider account identifiers, fees or settlement destinations
  from an untrusted client.
- Browser redirects are UX signals, not financial finality.
- Verified provider webhooks or controlled reconciliation establish provider
  outcomes.
- A settlement release is idempotent and cannot exceed captured funds minus
  refunds and previous releases.
- A disputed deal blocks settlement release until an authorized resolution.
- Unknown provider outcomes are reconciled before replacement operations are
  attempted.
- Financial state and its outbox event are committed atomically.

## PCI boundary

Stripe is the first payment provider. Card data is collected by Stripe-hosted
Checkout and never passes through PayGuard applications, APIs, logs or events.
Provider abstractions must keep Stripe-specific objects outside domain models.

## Trust boundaries

- User-facing clients call Deal Service through API Gateway.
- Deal Service issues trusted funding, release and refund commands to Payment
  Service.
- Payment Service independently resolves payee settlement eligibility through
  User Service.
- Service identity and OAuth scopes are derived from access tokens, never from
  request bodies.
- Kafka delivery is at least once; every consumer implements durable inbox or
  equivalent event-ID deduplication.

