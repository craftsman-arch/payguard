# Local development

This guide describes how to start the implemented PayGuard services and their
local dependencies.

## Requirements

- Java 17;
- Maven 3.9+ or the Maven wrapper included with each service;
- Docker Desktop with Docker Compose;
- Stripe test-mode credentials for end-to-end merchant onboarding;
- `cloudflared` when Stripe must deliver webhooks to the local environment.

## Environment

Create the local environment file from the repository root:

```powershell
Copy-Item .env.example .env
```

Fill the required values before starting the stack. The `.env` file is
local-only and must not be committed. Use only Stripe test-mode credentials.

When User Service runs from an IDE, provide the environment variables required
by its `application.yaml`. The Keycloak client secret must match the value in
the local realm import:

```text
KEYCLOAK_CLIENT_SECRET=payguard-secret
STRIPE_SECRET_KEY=<Stripe test secret key>
STRIPE_WEBHOOK_SECRET=<Stripe test webhook signing secret>
```

## Infrastructure

Start the local dependencies from the repository root:

```powershell
docker compose up -d
```

| Component | Address |
|---|---|
| Keycloak | http://localhost:8080 |
| API Gateway | http://localhost:8084 |
| User Service | http://localhost:8081 |
| Payment Service | http://localhost:8082 (when implemented and running) |
| Redpanda Console | http://localhost:8085 |
| Redis Insight | http://localhost:5540 |
| Mailpit | http://localhost:8025 |
| PostgreSQL | `localhost:5432` |
| Kafka | `localhost:9092` |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| Loki | http://localhost:3100 |
| Tempo | http://localhost:3200 |

Redpanda Console is a Kafka UI; the local broker remains Apache Kafka.

## Services

Each implemented service owns its Maven build and runtime configuration. Start
a service from its directory, for example:

```powershell
Set-Location services/user-service
mvn spring-boot:run
```

Run API Gateway separately from `services/api-gateway` when testing public
routes.

## Stripe webhook tunnel

Expose API Gateway to Stripe:

```powershell
cloudflared tunnel --url http://localhost:8084
```

Configure the generated destination in the Stripe test environment:

```text
https://<generated-host>/api/webhooks/stripe
```

The Gateway permits this webhook route without a JWT. User Service still
requires and verifies a valid Stripe signature.

## Kafka inspection

User Service currently publishes merchant lifecycle events with the following
delivery contract:

```text
topic: payguard.user-service.merchant-events
key: merchantId
delivery: at least once
```

Inspect topics and messages through Redpanda Console. Consumers must implement
idempotent processing by event ID.

## Local security rules

- Never commit `.env`, access tokens, Stripe keys or webhook secrets.
- Never publish passwords, action links, onboarding links or KYC documents to
  Kafka.
- Use only Stripe test accounts and synthetic test data.
