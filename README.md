# PayGuard

<p align="center">
  <img src="./payguard-logo.png" alt="PayGuard logo" width="240">
</p>

PayGuard is a protected-transaction platform for people and businesses that
need to exchange money after agreed conditions have been satisfied. A user may
fund one deal as its payer and receive funds in another as its payee. Payments
are collected through a hosted payment-provider experience, held from release
while the deal is in progress, and released or refunded after fulfilment,
acceptance or dispute resolution.

The platform is organized as independently deployable services with explicit
ownership of users, protected deals, financial operations and fraud decisions.
Stripe is the first payment provider. PayGuard does not collect raw card data
and does not claim to provide a regulated escrow account; the product model is
described as protected deals with conditional settlement.

PayGuard was initially designed as the payment component of a marketplace
ecosystem. Its first completed vertical slice therefore implements merchant
registration, Stripe Connect onboarding and payment eligibility. Further
domain analysis showed that the same financial and risk boundaries can support
a broader protected-transaction product in which users may participate as
either payer or payee. The next phase generalizes the original Merchant model
into platform users with optional settlement accounts before Deal, Payment and
Fraud services are implemented against the new boundaries.

## Service status

| Service | Status | Responsibility |
|---|---|---|
| API Gateway | Implemented; evolution planned | Public routing, JWT enforcement, CORS, correlation IDs and Redis-backed abuse protection |
| User Service | Implemented; refactoring planned | Current merchant identity and Stripe Connect lifecycle; evolving to platform users with optional settlement accounts |
| Deal Service | Planned | Offers, participants, fulfilment, cancellation, disputes and business authority to release or refund |
| Payment Service | Planned | Deal funding, hosted Checkout, provider webhooks, conditional settlement, refunds and financial history |
| Fraud Engine | Planned | Explainable risk decisions for payment initiation and settlement release |
| Notification Service | TBD | Omnichannel delivery of payment links and lifecycle notifications |
| Reconciliation Service | TBD | Provider comparison, discrepancy detection and controlled recovery workflows |

## Repository structure

```text
services/
  api-gateway/
  user-service/
  deal-service/            Planned
  payment-service/
  notification-service/
  reconciliation-service/
  fraud-engine/

libs/
  common/
  event-contracts/

docker/        Local infrastructure configuration
docs/          Implemented architecture, public contracts and explicit plans
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
- [Protected-transactions build plan](docs/plans/protected-transactions/README.md)
- [Planned service boundaries](docs/plans/protected-transactions/service-boundaries.md)
- [User Service application architecture](docs/architecture/user-service/application-architecture.md)
- [Merchant lifecycle](docs/architecture/user-service/merchant-lifecycle.md)
- [Merchant registration](docs/architecture/user-service/registration-sequence.md)
- [Stripe webhook processing](docs/architecture/user-service/webhook-processing-sequence.md)
- [Transactional outbox operations](docs/architecture/user-service/outbox-operations.md)
- [Merchant payment status Kafka contract](docs/contracts/kafka/merchant-payment-account-status-changed-v1.md)
