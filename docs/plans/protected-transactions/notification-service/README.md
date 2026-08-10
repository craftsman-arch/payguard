# Notification Service build plan

## Goal

Deliver payment links and protected-deal lifecycle notifications through
digital channels without owning financial or deal state.

## 01 - Event consumption foundation

- Consume versioned notification requests with durable inbox deduplication.
- Validate templates, channel policy and recipient-reference authorization.
- Resolve contact details through an authorized profile boundary rather than
  embedding PII in broad Kafka events.
- Persist delivery attempts and provider-neutral outcomes.

## 02 - Email and SMS delivery

- Deliver expiring hosted Checkout links by email and SMS.
- Support deal funded, delivered, disputed, released and refunded templates.
- Add provider timeouts, circuit breakers and bounded retries.
- Prevent secrets and message bodies from appearing in logs or metrics.

## 03 - Additional digital channels

- Add web-chat, social-messaging and agent-assisted adapters behind a common
  delivery port.
- Keep channel choice separate from payment execution.
- Make duplicate delivery policy explicit per notification type.

## Excluded

- telephony DTMF card capture;
- raw payment-card collection;
- ownership of Checkout sessions or deal transitions.

