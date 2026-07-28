# Stripe webhook processing

## Request and transaction flow

```mermaid
sequenceDiagram
    participant Stripe
    participant Gateway as API Gateway
    participant Controller as User Service
    participant Verifier as Signature verifier
    participant Retry as RetryTemplate
    participant Tx as Transaction processor
    participant Dedup as processed_stripe_events
    participant Handler as account.updated handler
    participant Merchant as Merchant repository
    participant Outbox as outbox_events

    Stripe->>Gateway: POST /api/webhooks/stripe
    Gateway->>Controller: POST /api/v1/webhooks/stripe
    Controller->>Verifier: Verify signature against raw body
    Verifier-->>Controller: Valid
    Controller->>Controller: Parse provider-neutral webhook
    Controller->>Retry: Execute

    loop Initial attempt and bounded concurrency retries
        Retry->>Tx: process(webhook) in REQUIRES_NEW
        Tx->>Dedup: INSERT eventId ON CONFLICT DO NOTHING
        alt Event already processed
            Dedup-->>Tx: Not claimed
            Tx-->>Retry: Ignore duplicate
        else Event claimed
            Tx->>Handler: Dispatch typed payload
            Handler->>Merchant: Load by paymentAccountId
            Handler->>Handler: Apply timestamp and domain policy
            Handler->>Merchant: saveAndFlush with expected revision
            alt Revision conflict
                Merchant-->>Tx: Optimistic locking failure
                Tx-->>Retry: Roll back dedup, Merchant and outbox
            else State committed
                Handler->>Outbox: Insert domain event when state changed
                Tx-->>Retry: Commit
            end
        end
    end

    Controller-->>Gateway: 200
    Gateway-->>Stripe: 200
```

## Processing rules

1. API Gateway permits unauthenticated access only to the Stripe webhook route.
2. User Service verifies the Stripe signature using the unmodified request
   payload.
3. Only supported provider events are dispatched.
4. `processed_stripe_events.event_id` provides durable deduplication.
5. Unknown payment accounts are logged and safely ignored.
6. Older account snapshots are ignored.
7. Merchant persistence uses JPA optimistic locking on `revision`.
8. A concurrency conflict rolls back the complete attempt and `RetryTemplate`
   reloads state in a new transaction.
9. Merchant state and outbox events commit atomically.
10. Provider-specific requirements are normalized before leaving User Service.

## Asynchronous publication

```mermaid
sequenceDiagram
    participant Publisher as Outbox publisher
    participant DB as PostgreSQL
    participant Kafka

    Publisher->>DB: Claim earliest eligible events
    DB-->>Publisher: Batch with claimId and lease
    loop Each claimed event
        Publisher->>Kafka: Send using aggregateId key
        alt Kafka acknowledgement
            Publisher->>DB: Mark published if id + claimId still owned
        else Confirmed failure
            Publisher->>DB: Increment attempts and reschedule
        end
    end
```

Publication is at least once and runs outside the business transaction. Later
events of one merchant cannot bypass an unpublished predecessor, including an
exhausted head event. Other merchants remain independent.

The public wire contract is documented in
[`merchant-payment-account-status-changed-v1.md`](../../contracts/kafka/merchant-payment-account-status-changed-v1.md).
