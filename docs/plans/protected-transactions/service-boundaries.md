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
- Every payment, release and refund belongs to a specific protected deal. The
  platform does not support free-form peer-to-peer money transmission.
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

## Market boundaries

- A deal is accepted only when its payer market, payee market, currency,
  payment method and settlement route form an explicitly supported
  combination.
- Domestic support for one country does not imply cross-border support.
- Market policy owns supported currencies, deal categories, payment methods,
  limits and timing rules.
- Provider routing owns provider accounts, regions and supported funds flows;
  provider credentials and routing identifiers never enter the domain model.
- New markets are disabled by default and enabled through an explicit policy.
- Provider-hosted onboarding and verification handle provider requirements,
  but do not replace PayGuard's own market, risk or legal assumptions.

## Verification and onboarding ownership

PayGuard deliberately uses provider-hosted or provider-embedded onboarding.
KYC/KYB fields, accepted identity documents, beneficial-owner and company
representative requirements, verification thresholds and remediation steps
vary by country, business type, requested capability and risk level. They can
also change after an account has already been onboarded.

The payment provider owns:

- collection and verification of provider-specific identity and business data;
- document upload, verification and remediation flows;
- provider capability activation and suspension;
- communication of changing requirements through its onboarding experience
  and signed lifecycle events.

User Service owns:

- the provider-neutral settlement-account lifecycle;
- normalized capabilities, eligibility and required action;
- durable processing of signed provider lifecycle events;
- the internal settlement context consumed by trusted services.

PayGuard market and risk policies still own which countries, currencies, deal
categories, limits and settlement routes the platform permits. Successful
provider verification is necessary for receiving funds, but does not by itself
authorize participation in every PayGuard market or deal.

Raw provider requirement names and KYC/KYB payloads are not domain contracts
and are not exposed to downstream services. This prevents changing provider
policies from forcing equivalent changes through PayGuard's service contracts.

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

