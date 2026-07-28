# Merchant payment account status changed v1

## Status

- Contract status: active
- Producer and owner: PayGuard User Service
- Event type: `merchant.payment-account-status-changed.v1`
- Default topic: `payguard.user-service.merchant-events`
- Serialization: UTF-8 JSON
- Delivery guarantee: at least once

This document is the language-neutral wire contract. Java records in User
Service are its current implementation, not a substitute for the contract.

## Purpose

`merchant.payment-account-status-changed.v1` reports a business-significant
change in a merchant's normalized payment-account state.

User Service remains the authoritative source of this state. Consumers must not
derive their own payment eligibility policy from Stripe fields.

Repeated Stripe provider updates that do not change normalized state do not
produce this event.

## Topic and partitioning

| Property | Value |
|---|---|
| Topic | `payguard.user-service.merchant-events` |
| Topic owner | User Service |
| Default partitions | `6` |
| Message key | Merchant ID as a UUID string |
| Value | Complete event envelope as UTF-8 JSON |

The message key is the same merchant ID carried by `data.merchantId`. All
events for one merchant therefore map to one Kafka partition.

Consumers must not depend on a particular partition number or on the default
partition count.

## Kafka headers

Header names and values are case-sensitive. Values are UTF-8 strings.

| Header | Required | Description |
|---|---:|---|
| `eventId` | yes | Same UUID as JSON envelope field `id` |
| `eventType` | yes | Same value as JSON envelope field `type` |
| `correlationId` | no | Same value as `correlationId`; omitted when unavailable |

Consumers should use the JSON envelope as the durable event representation.
Headers exist for routing, tracing and filtering without deserializing the
value.

## Envelope

```json
{
  "id": "d3a170e8-3750-413b-aa18-cddce391ee46",
  "type": "merchant.payment-account-status-changed.v1",
  "source": "user-service",
  "time": "2026-07-28T07:53:08.868017900Z",
  "subject": "merchant/5bebadec-df6b-4f95-a017-bcc92b764b3e",
  "correlationId": "f887a516bcde24d3575f69e1550fcb46",
  "data": {
    "merchantId": "5bebadec-df6b-4f95-a017-bcc92b764b3e",
    "previousState": {
      "paymentAccountStatus": "PENDING_ONBOARDING",
      "requiredAction": "WAIT_FOR_REVIEW",
      "readyForPayments": false,
      "eligibleForDestinationCharges": false
    },
    "currentState": {
      "paymentAccountStatus": "ACTIVE",
      "requiredAction": "NONE",
      "readyForPayments": true,
      "eligibleForDestinationCharges": true
    }
  }
}
```

### Envelope fields

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | UUID string | yes | Stable event identity; unchanged across publication retries |
| `type` | string | yes | `merchant.payment-account-status-changed.v1` |
| `source` | string | yes | `user-service` |
| `time` | RFC 3339 timestamp | yes | Time at which the domain change occurred |
| `subject` | string | yes | `merchant/{merchantId}` |
| `correlationId` | string or null | no | Request/trace correlation identifier when available |
| `data` | object | yes | Version 1 event payload |

The contract version is part of `type`. There is no separate JSON
`eventVersion` field in v1.

## Payload

| Field | Type | Required | Description |
|---|---|---:|---|
| `merchantId` | UUID string | yes | Merchant aggregate ID |
| `previousState` | `PaymentState` | yes | Normalized state before the change |
| `currentState` | `PaymentState` | yes | Normalized state after the change |

### PaymentState

| Field | Type | Required | Description |
|---|---|---:|---|
| `paymentAccountStatus` | enum | yes | Normalized payment-account lifecycle status |
| `requiredAction` | enum | yes | Action expected from the merchant or platform |
| `readyForPayments` | boolean | yes | Whether PayGuard considers the merchant ready to accept payments |
| `eligibleForDestinationCharges` | boolean | yes | Whether destination charges may target this merchant |

### PaymentAccountStatus

| Value | Meaning |
|---|---|
| `PENDING_ONBOARDING` | Onboarding or provider requirements are incomplete |
| `ACTIVE` | Payment account is active according to User Service policy |
| `RESTRICTED` | Account has a recoverable restriction |
| `DISABLED` | Account has a terminal or support-managed restriction |

### PaymentAccountRequiredAction

| Value | Meaning |
|---|---|
| `CONTINUE_ONBOARDING` | Merchant must continue Stripe-hosted onboarding |
| `WAIT_FOR_REVIEW` | Provider review is pending; no immediate merchant action |
| `CONTACT_SUPPORT` | Merchant or operator must contact PayGuard support |
| `NONE` | No payment-account action is currently required |

Consumers must use the booleans supplied by User Service rather than recreating
eligibility rules from enum combinations.

## Delivery and ordering semantics

Publication is transactional-outbox based and at least once:

1. Merchant state and the outbox row commit in one PostgreSQL transaction.
2. A background publisher sends the stored envelope to Kafka.
3. The outbox row is marked published only after Kafka acknowledges the send.
4. Failure before acknowledgement schedules the same event `id` for retry.

Duplicate Kafka records are possible around failure boundaries. Consumers must
deduplicate by event `id` within their own consumer scope.

User Service preserves strict publication order per merchant:

- outbox `sequence_number` defines local commit order;
- only the earliest unpublished event of a merchant is eligible for claim;
- an exhausted unpublished head event blocks later events of that merchant;
- events for other merchants continue to publish;
- Kafka uses merchant ID as the key and preserves accepted record order within
  the resulting partition.

An exhausted event is recovered with the same event `id`; it is not skipped.
A late duplicate of an older event remains possible when a publisher loses its
lease around a Kafka acknowledgement boundary. Such a record carries the same
event `id`; consumers must discard it through idempotency handling. A later
first-time event is never made eligible merely because its unpublished
predecessor exhausted retries.

## Consumer processing requirements

A consumer that changes its own database should implement an inbox or an
equivalent atomic idempotency mechanism:

```text
begin local transaction
    register (consumerName, eventId) under a unique constraint
    if already registered: do nothing
    otherwise: apply the business effect
commit local transaction
acknowledge Kafka offset
```

The objective is an exactly-once business effect in the consumer's local
database, not exactly-once delivery from Kafka.

Consumers must:

- process only supported event types and contract versions;
- use `id` as the idempotency identifier;
- treat unknown JSON object fields as ignorable;
- fail or quarantine messages with missing required fields or unknown enum
  values;
- avoid acknowledging a record when its local transaction rolled back;
- keep offset management consistent with their processing model.

## Compatibility rules

Changes compatible with v1:

- adding an optional field;
- adding documentation or clarifying existing semantics without changing them;
- adding a new event type for a different business fact.

Changes requiring a new event version:

- removing or renaming a field;
- changing a field type;
- changing the meaning of an existing field or enum value;
- making an optional field required;
- changing message-key semantics;
- restructuring the envelope or payload;
- adding an enum value unless all consumers are explicitly forward-compatible
  with unknown values.

A breaking revision uses a new type, for example:

```text
merchant.payment-account-status-changed.v2
```

The Java class may keep the semantic name
`MerchantPaymentAccountStatusChanged` under a versioned package such as
`contracts.merchant.v2`.

Before the first independent consumer is released, evaluate promoting this
document to an executable JSON Schema, Avro or Protobuf contract. The selected
language-neutral schema should become the source of truth and may later be
packaged in a shared `payguard-event-contracts` module.

## Data classification

This event must not contain:

- passwords or temporary passwords;
- invitation or account-action tokens;
- Keycloak action links;
- Stripe onboarding links;
- Stripe secret or publishable keys;
- webhook signatures;
- raw provider payloads;
- identity documents, KYC documents or bank documents;
- unnecessary personal or contact information.

Merchant ID is the partition key. Email must never be used as a Kafka key.

Event payloads must not be written to ordinary application logs.
