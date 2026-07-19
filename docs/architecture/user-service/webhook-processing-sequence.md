# Stripe Webhook Processing

```mermaid
sequenceDiagram

participant Stripe

participant WebhookController

participant SignatureVerifier

participant EventDispatcher

participant AccountUpdatedHandler

participant MerchantRepository

Stripe->>WebhookController: POST /api/v1/webhooks/stripe

WebhookController->>SignatureVerifier: Verify Signature

SignatureVerifier-->>WebhookController: OK

WebhookController->>EventDispatcher: Dispatch Event

EventDispatcher->>AccountUpdatedHandler: account.updated

AccountUpdatedHandler->>MerchantRepository: findByPaymentAccountId()

MerchantRepository-->>AccountUpdatedHandler: Merchant

AccountUpdatedHandler->>MerchantRepository: update(ACTIVE)
```

## Processing Steps

1. Verify Stripe webhook signature.
2. Validate timestamp.
3. Deserialize webhook payload.
4. Dispatch event.
5. Execute matching event handler.
6. Activate merchant.