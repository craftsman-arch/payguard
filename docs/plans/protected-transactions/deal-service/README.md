# Deal Service build plan

## Goal

Own protected agreements between PayGuard users without taking ownership of
payment-provider operations or financial truth.

## 01 - Service and deal foundation

- Create a separately deployable Spring Boot service and PostgreSQL schema.
- Introduce `ProtectedDeal`, `DealId`, payer/payee participants, agreed Money,
  bounded description, expiry and optimistic revision.
- Define deal-scoped `PAYER` and `PAYEE` roles.
- Implement `DRAFT`, `OFFERED`, `ACCEPTED`, `AWAITING_PAYMENT`, `FUNDED`,
  `DELIVERED`, `DISPUTED`, `COMPLETED`, `CANCELED` and `REFUNDED` invariants.
- Add OAuth2 resource-server security, Flyway, OpenAPI and observability.

## 02 - Offer and acceptance

- Create, view, offer, accept, reject and expire a deal.
- Ensure the authenticated participant owns every user-scoped command.
- Resolve participant eligibility through User Service.
- Store immutable agreement terms after acceptance.
- Use idempotency keys and database constraints for externally retried commands.

## 03 - Funding orchestration

- Request deal funding from Payment Service after acceptance.
- Consume versioned `payment.funded` and `payment.failed` events idempotently.
- Never mark a deal funded from a browser redirect.
- Keep provider references and Checkout URLs outside the Deal aggregate.

## 04 - Fulfilment and release authority

- Allow the payee to mark a funded deal delivered.
- Allow only the payer to accept delivery.
- Emit an idempotent settlement-release command after valid acceptance.
- Consume settlement lifecycle events and complete the deal only after financial
  release is confirmed.
- Record bounded delivery references and audit actors/timestamps.

## 05 - Cancellation and dispute

- Support cancellation before funding without provider money movement.
- Open a dispute for a funded, unreleased deal and block automatic release.
- Allow `RISK_ANALYST` to resolve a dispute to release or refund.
- Keep every decision, actor, reason, correlation ID and timestamp auditable.
- Consume refund/release results idempotently.

## Nice to have

- Built-in deal chat for communication between payer and payee.
- File and image attachments for delivery evidence, cancellation discussions
  and disputes.

If implemented later, binary content belongs in object storage. Deal Service
stores only bounded metadata and opaque attachment references. Chat and file
content must not be embedded in Kafka lifecycle events.

## Excluded from the first vertical slice

- wallet balances;
- partial advance payment;
- automated courier verification;
- ratings and marketplace catalog;
- automatic dispute resolution;
- regulated escrow claims.

