# Payment Service

## Goal

The `payment-service` is responsible for the complete payment lifecycle.

It owns payment processing, Stripe payment APIs, event publishing, and integrations with downstream services.

It does **not** own merchants or user identities.

Merchant onboarding and Stripe Connect account creation belong to `user-service`.

---

# Responsibilities

The payment-service owns:

- Payment lifecycle
- Payment authorization
- Payment capture
- Refunds
- Payouts
- Payment webhooks
- Stripe payment integration
- Ledger events
- Fraud integration
- Payment events
- Outbox Pattern

---

# Not Responsible For

The payment-service does not own:

- Merchant registration
- Merchant lifecycle
- Merchant onboarding
- Identity Provider integration
- Stripe Connect onboarding
- Merchant activation

Those responsibilities belong to `user-service`.

---

# Payment Lifecycle

```text
CREATED
    │
    ▼
AUTHORIZED
    │
    ▼
CAPTURED
    │
    ▼
SETTLED
```

Possible alternative flows:

```text
AUTHORIZED
    │
    ▼
CANCELLED
```

or

```text
CAPTURED
    │
    ▼
REFUNDED
```

---

# Payment Flow

```text
Client

    │

    ▼

POST /payments

    │

    ▼

PaymentService

    │

    ├── Validate merchant

    ├── Run fraud checks

    ├── Authorize payment

    ├── Persist payment

    ├── Save Outbox Event

    └── Commit Transaction
```

After commit:

```text
Outbox

    │

    ▼

Kafka
```

---

# Outbox Pattern

Every business event is stored in the same database transaction as the payment.

```text
Database Transaction

Payment

+

Outbox Event

↓

COMMIT
```

A background publisher delivers events to Kafka.

This guarantees:

- no lost events
- atomic persistence
- eventual consistency

---

# Published Events

Examples:

```text
PaymentCreated

PaymentAuthorized

PaymentCaptured

PaymentFailed

PaymentRefunded

PaymentCancelled
```

These events may be consumed by:

- fraud-engine
- notification-service
- analytics
- ledger
- reporting

---

# Fraud Integration

Fraud analysis is asynchronous.

Example:

```text
PaymentCreated

↓

Kafka

↓

Fraud Engine

↓

FraudDecisionReceived
```

Possible outcomes:

```text
APPROVED

DECLINED

REVIEW
```

---

# Saga

Payment processing is a long-running business process.

Example:

```text
Create Payment

↓

Authorize Card

↓

Fraud Check

↓

Capture Payment

↓

Update Ledger

↓

Notify Merchant
```

If a step fails, compensating actions may be executed.

Example:

```text
Capture Failed

↓

Void Authorization
```

or

```text
Fraud Declined

↓

Cancel Payment
```

The Saga coordinates the business process while maintaining eventual consistency.

---

# Stripe Integration

The payment-service communicates directly with Stripe.

Responsibilities include:

- PaymentIntents
- Charges
- Refunds
- Payouts
- Balance Transactions

It also processes Stripe payment webhooks.

Examples:

- payment_intent.succeeded
- payment_intent.payment_failed
- charge.refunded
- payout.paid

Merchant onboarding webhooks are intentionally **not** handled here.

---

# Idempotency

Every external event must be processed exactly once.

Examples:

- Stripe webhook id
- Kafka message id

Duplicate messages must not produce duplicate business actions.

---

# Architecture

```text
REST API

        │

        ▼

Application

        │

        ▼

Domain

        │

        ▼

Persistence

        │

        ▼

Outbox

        │

        ▼

Kafka
```

External integrations:

```text
Stripe

Kafka

Fraud Engine

Notification Service

Ledger
```

---

# Design Principles

- Payment owns the payment lifecycle.
- Every business event is published through the Outbox Pattern.
- Kafka is the integration backbone.
- Long-running workflows are coordinated using Saga.
- Consumers must be idempotent.
- Business rules remain inside the domain model.
- Infrastructure is responsible only for communication with external systems.