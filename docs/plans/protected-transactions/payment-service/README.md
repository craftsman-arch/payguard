# Payment Service build plan

## Goal

Own the complete financial lifecycle of funding a protected deal, holding
settlement pending business authorization, releasing funds to an eligible payee
and executing refunds without exposing card data to PayGuard.

## 01 - Financial domain and persistence foundation

- Keep provider-neutral `Money` and `CurrencyCode` primitives.
- Introduce `Payment`, `PaymentAttempt`, `Settlement` and `Refund` lifecycles.
- Add PostgreSQL/Flyway persistence and database compare-and-set revisions.
- Store provider references and immutable economic allocation snapshots.
- Define application ports for User Service, Deal Service, Fraud Engine and the
  payment provider.

## 02 - Idempotent deal funding intake

- Accept funding commands only from an authorized Deal Service client.
- Derive caller identity from OAuth rather than request data.
- Require `DealId`, payer reference, payee `UserId`, agreed Money, initiation
  channel and an idempotency key.
- Resolve payee settlement context through User Service.
- Reject provider account IDs, arbitrary fees, card data and unbounded metadata.
- Guarantee that concurrent replay creates one Payment.

## 03 - Funding fraud assessment

- Call Fraud Engine before provider execution.
- Persist assessment ID, score, decision, model/feature versions and bounded
  reason codes.
- Support `ALLOW`, `REVIEW` and `BLOCK`.
- Degrade to `REVIEW`, never silent approval, when risk assessment is unavailable.
- Prevent reviewed or blocked payments from reaching Stripe.

## 04 - Stripe-hosted funding

- Create hosted Checkout on the PayGuard platform account.
- Keep raw card data outside PayGuard.
- Use stable provider idempotency based on payment and attempt IDs.
- Treat redirects as non-final and verified webhooks as authoritative.
- Model ambiguous creation as `UNKNOWN` and reconcile before replacement.
- Do not transfer funds to the payee during initial collection.

## 05 - Verified webhook and payment events

- Verify Stripe signatures over the raw request body.
- Deduplicate provider events durably and reject stale/invalid transitions.
- Correlate provider objects through trusted internal metadata.
- Publish payment lifecycle facts through a transactional outbox.
- Exclude Checkout URLs, PII and provider payloads from Kafka.

## 06 - Conditional settlement

- Accept release only from authorized Deal Service commands.
- Revalidate payee settlement eligibility immediately before release.
- Perform a separate release-risk assessment.
- Create one idempotent Stripe Transfer for an authorized amount.
- Track transfer attempts, unknown outcomes and reconciliation state.
- Publish settlement lifecycle events only after durable state transitions.

## 07 - Refunds and reversals

- Support idempotent full and partial refunds.
- Prevent pending plus completed refunds from exceeding captured funds.
- Refund unreleased funds directly.
- Model transfer reversal when previously released funds must be recovered.
- Preserve immutable audit and provider references.
- Use verified webhook/reconciliation finality where supported.

## 08 - Omnichannel initiation

- Support `WEB_CHECKOUT`, `PAYMENT_LINK`, `EMAIL`, `SMS`, `WEB_CHAT`,
  `SOCIAL_MESSAGING` and `AGENT_ASSISTED` as initiation metadata.
- Use the same hosted Checkout financial flow for every digital channel.
- Publish a bounded notification request rather than sending messages directly.
- Never put recipient contact details in Kafka keys or metric labels.

## 09 - Reconciliation and operations

- Expose scoped reconciliation facts for charges, transfers, refunds and
  reversals.
- Recover missing webhooks and unknown provider outcomes without duplicate
  money movement.
- Add outbox recovery, retention, metrics, alerts and runbooks.
- Verify concurrency with multiple application replicas in integration tests.

