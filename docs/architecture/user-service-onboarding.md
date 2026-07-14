# User Service - Merchant Onboarding Flow

## Goal

The `user-service` is responsible for the merchant lifecycle, user identity provisioning, and payment account onboarding.

It does **not** process payments, refunds, disputes, or payment events. Those responsibilities belong to `payment-service`.

---

# Merchant Lifecycle

```text
PENDING
    │
    ▼
ACTIVE
    │
    ▼
SUSPENDED
```

A merchant is created in the `PENDING` state.

The merchant becomes `ACTIVE` only after successful payment provider onboarding.

---

# Use Case 1 — Register Merchant

Synchronous operation.

```text
POST /api/v1/merchants
        │
        ▼
RegisterMerchantService
        │
        ├── Validate request
        ├── Check email uniqueness
        ├── Merchant.register()
        ├── IdentityGateway.createUser()
        ├── merchant.linkIdentityProvider()
        ├── merchantRepository.add()
        └── Return 201 Created
```

Responsibilities:

- create merchant
- provision user in Keycloak
- persist merchant
- return merchant id

No interaction with Stripe occurs during registration.

---

# Use Case 2 — Connect Payment Provider

Synchronous operation.

```text
POST /api/v1/merchants/{id}/payment-account
        │
        ▼
ConnectPaymentProviderService
        │
        ├── Load Merchant
        ├── Validate current state
        ├── PaymentGateway.createAccount()
        ├── merchant.linkPaymentProvider()
        ├── merchantRepository.update()
        └── Return Stripe onboarding URL
```

Stripe returns:

- Stripe Account ID
- Account onboarding URL

The merchant remains in the `PENDING` state.

---

# Payment Provider Onboarding

The user completes onboarding directly on Stripe.

Typical onboarding includes:

- identity verification
- business information
- tax information
- bank account
- compliance checks

The application does not participate in this process.

---

# Stripe Webhooks

Stripe notifies the application asynchronously.

Example events:

- account.updated
- capabilities.updated
- requirements.updated

Webhook flow:

```text
Stripe
    │
    ▼
StripeWebhookController
    │
    ▼
StripeWebhookService
    │
    ▼
Merchant.completePaymentOnboarding(...)
    │
    ▼
merchantRepository.update()
```

The domain decides whether onboarding is complete.

Example:

```text
charges_enabled == true
details_submitted == true
```

↓

```text
Merchant.activate()
```

↓

```text
ACTIVE
```

Business rules remain inside the domain model.

---

# Responsibilities

## user-service

Responsible for:

- Merchant lifecycle
- Merchant registration
- Identity Provider integration
- Stripe Connect account creation
- Merchant onboarding
- Stripe account webhooks
- Merchant activation

Not responsible for:

- payment processing
- refunds
- disputes
- payouts
- fraud detection
- ledger
- payment events

---

## payment-service

Responsible for:

- payment authorization
- payment capture
- refunds
- payouts
- disputes
- ledger
- fraud integration
- Outbox Pattern
- Kafka integration
- Saga (when required)

---

# Architectural Notes

## Identity Provider

Identity provisioning is synchronous.

A successful registration guarantees that:

- Merchant exists
- Identity Provider user exists

If identity provisioning fails, the registration fails.

---

## Payment Provider

Payment onboarding is split into two phases.

### Phase 1

Synchronous.

Create Stripe Connect Account.

### Phase 2

Asynchronous.

Stripe sends webhook events as onboarding progresses.

The merchant is activated only after the domain determines onboarding is complete.

---

# Design Principles

- Domain Model owns business rules.
- Infrastructure integrates with external systems.
- Registration is synchronous for better user experience.
- Stripe onboarding follows Stripe's asynchronous webhook model.
- Business state changes only through aggregate methods.
- External providers never modify persistence directly.