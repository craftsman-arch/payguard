# PayGuard

PayGuard is a marketplace payment platform organized as independently
deployable services. Its architecture separates identity and merchant
management, payment processing, notifications, reconciliation and fraud
analysis into explicit service boundaries.

## Repository structure

```text
services/
  api-gateway/
  user-service/
  payment-service/
  notification-service/
  reconciliation-service/
  fraud-engine/

libs/
  common/
  event-contracts/

docker/        Local infrastructure configuration
docs/          Implemented architecture and public contracts
infra/         Helm and Terraform assets
```

Not every service directory represents a completed service. Implemented behavior
and planned work are intentionally documented separately.

## Current User Service capabilities

- self-service merchant registration with Keycloak password policy, email
  verification and OTP;
- Stripe Connect account creation and hosted onboarding;
- normalized payment-account status and required-action policy;
- merchant readiness and destination-charge eligibility;
- signed Stripe webhook processing;
- durable webhook deduplication;
- optimistic Merchant concurrency through JPA revision checks;
- transactional outbox and at-least-once Kafka publication;
- exhausted-event recovery, outbox operational metrics and bounded retention;
- protected internal payment-context API;
- authorization-code flow with PKCE for the merchant browser client.

## Requirements

- Java 17;
- Maven 3.9+ or the included Maven wrapper;
- Docker Desktop with Docker Compose;
- Stripe test-mode credentials for end-to-end onboarding;
- `cloudflared` only when receiving Stripe webhooks from the public internet.

## Local infrastructure

Create the local environment file:

```powershell
Copy-Item .env.example .env
```

Fill all required values. Secrets in `.env` are local-only and must never be
committed. Stripe credentials must be test-mode credentials.

Start infrastructure:

```powershell
docker compose up -d
```

Useful endpoints:

| Component | Address |
|---|---|
| Keycloak | http://localhost:8080 |
| API Gateway | http://localhost:8084 |
| User Service | http://localhost:8081 |
| Redpanda Console | http://localhost:8085 |
| Mailpit | http://localhost:8025 |
| PostgreSQL | `localhost:5432` |
| Kafka | `localhost:9092` |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| Loki | http://localhost:3100 |
| Tempo | http://localhost:3200 |

Redpanda Console is only a UI. The local broker remains Apache Kafka.

## Running User Service

From the service directory:

```powershell
Set-Location services/user-service
mvn spring-boot:run
```

When running from an IDE, configure the environment variables required by
`application.yaml`. `KEYCLOAK_CLIENT_SECRET` must have the same value in User
Service and in the local realm import:

```text
KEYCLOAK_CLIENT_SECRET=payguard-secret
```

Also provide:

```text
STRIPE_SECRET_KEY=<Stripe test secret key>
STRIPE_WEBHOOK_SECRET=<Stripe test webhook signing secret>
```

Run API Gateway separately from `services/api-gateway` when testing public
routes.

## Stripe webhook tunnel

Expose API Gateway:

```powershell
cloudflared tunnel --url http://localhost:8084
```

Configure the generated HTTPS destination in Stripe:

```text
https://<generated-host>/api/webhooks/stripe
```

Only the Gateway webhook route is public without JWT. User Service still
requires a valid Stripe signature.

## Kafka

Merchant lifecycle events use:

```text
topic: payguard.user-service.merchant-events
key: merchantId
delivery: at least once
```

Inspect topics and messages through [Redpanda Console](http://localhost:8085).
Consumers must implement idempotent processing by event ID.

## Documentation

- [Architecture overview](docs/architecture/README.md)
- [User Service application architecture](docs/architecture/user-service/application-architecture.md)
- [Merchant lifecycle](docs/architecture/user-service/merchant-lifecycle.md)
- [Merchant registration](docs/architecture/user-service/registration-sequence.md)
- [Stripe webhook processing](docs/architecture/user-service/webhook-processing-sequence.md)
- [Transactional outbox operations](docs/architecture/user-service/outbox-operations.md)
- [Merchant payment status Kafka contract](docs/contracts/kafka/merchant-payment-account-status-changed-v1.md)

Architecture documents describe implemented behavior. Future scope and
reliability work are maintained in the separate User Service rebuild plans.

## Security notes

- Never commit `.env`, access tokens, Stripe keys or webhook secrets.
- Never publish passwords, invitation tokens, action links, onboarding links or
  KYC documents to Kafka.
- Use only test-mode Stripe accounts and test data in local development.
