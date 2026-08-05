# PayGuard

PayGuard is a payment backend for an online marketplace where independent
merchants sell goods or services to buyers. It connects merchants to Stripe,
checks whether they are allowed to accept payments, coordinates payment and
risk decisions, and provides the operational records needed for notifications
and reconciliation.

The project is organized as independently deployable services with explicit
ownership of identity and merchant data, payments, fraud analysis,
notifications and reconciliation. The currently implemented flow covers
merchant registration, Stripe Connect onboarding and payment eligibility.
Payment processing is the next active implementation phase.

## Service status

| Service | Status | Responsibility |
|---|---|---|
| API Gateway | Implemented | Public routing, JWT enforcement, CORS, correlation IDs and Redis-backed limits for public registration endpoints |
| User Service | Implemented | Merchant identity and profile, Stripe Connect onboarding, payment eligibility and merchant lifecycle events |
| Payment Service | In development | Payment lifecycle, trusted merchant resolution, risk authorization, Stripe payment execution, refunds and payment history |
| Fraud Engine | Planned | Risk evaluation and explainable payment decisions; it does not own payment state |
| Notification Service | Planned | Email and other user-facing notifications driven by integration events |
| Reconciliation Service | Planned | Comparison of PayGuard records with provider settlements, discrepancies and operational follow-up |

PayGuard is not the marketplace storefront and does not manage buyer profiles,
catalogues or orders. The marketplace backend authenticates the buyer and sends
PayGuard a buyer reference together with the checkout context required to
process a payment.

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

<strong><u>Not every service directory represents a completed service.
Implemented behavior and planned work are intentionally documented
separately.</u></strong>

## Development approach

The project is developed in incremental vertical slices. Unit and integration
tests are added as domain rules, persistence contracts and service boundaries
become stable. Highly volatile functionality is validated during development
but is not given extensive automated coverage until its design settles, which
avoids repeatedly rewriting tests together with evolving production code.

Broader cross-service testing, CI quality gates, security and dependency
scanning, load testing, deployment manifests and AWS deployment are planned for
the final integration phase, after the core services have been implemented.

## Documentation

- [Local development](docs/local-development.md)
- [Architecture overview](docs/architecture/README.md)
- [User Service application architecture](docs/architecture/user-service/application-architecture.md)
- [Merchant lifecycle](docs/architecture/user-service/merchant-lifecycle.md)
- [Merchant registration](docs/architecture/user-service/registration-sequence.md)
- [Stripe webhook processing](docs/architecture/user-service/webhook-processing-sequence.md)
- [Transactional outbox operations](docs/architecture/user-service/outbox-operations.md)
- [Merchant payment status Kafka contract](docs/contracts/kafka/merchant-payment-account-status-changed-v1.md)
