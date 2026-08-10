# Protected transactions build plan

This directory describes the planned evolution of PayGuard from a
marketplace-specific payment platform into a protected-transaction platform.

Documents under `docs/architecture` remain the source of truth for behavior
that is already implemented. This plan describes intended changes and must not
be treated as a deployed runtime contract.

## Product model

A PayGuard user may be the payer in one deal and the payee in another. Payer
and payee are therefore deal-scoped roles rather than permanent identity-provider
roles.

PayGuard supports C2C/P2P commerce: one user pays another for a specific good
or service governed by a protected deal. It does not provide general-purpose
peer-to-peer money transmission, stored-value wallets or transfers without an
underlying commercial agreement.

PayGuard protects the financial part of an agreement:

```text
offer -> acceptance -> funding -> delivery -> acceptance or dispute
                                      |              |
                                      |              +-> release or refund
                                      +-> settlement remains on hold
```

PayGuard does not claim to provide a regulated escrow account. Product and
technical documentation use `protected deal` and `conditional settlement`.

## Market rollout

PayGuard is deployed market by market. A country is enabled only after its
currencies, provider capabilities, settlement routes, identity requirements,
payment methods, transaction limits and regulatory assumptions have been
explicitly configured.

```text
MVP:
GB + GBP + cards + domestic deals

Expansion 1:
IE + EUR + cards + UK/EEA routing

Expansion 2:
NL + EUR + iDEAL

Expansion 3:
AU + AUD + isolated provider region
```

The rollout is intentionally evolutionary. Ireland introduces a new currency
and cross-region routing, the Netherlands adds an asynchronous local payment
method, and Australia requires an isolated provider region. Availability in a
provider's country list alone is not sufficient to enable a PayGuard market.

## Service map

| Service | Planned responsibility |
| --- | --- |
| User Service | Platform users, identity linkage and optional settlement accounts |
| Deal Service | Agreement, participants, fulfilment, cancellation and dispute authority |
| Payment Service | Funding, provider execution, settlement, refunds and financial truth |
| Fraud Engine | Risk assessment for funding and settlement release |
| Notification Service | Omnichannel delivery of links and lifecycle notifications |
| Reconciliation Service | Provider-to-ledger comparison and discrepancy workflow |
| API Gateway | Public routing, authentication and abuse protection |

## Dependency order

```text
00 Service boundaries and invariants
        |
        v
01 User and settlement-account refactoring
        |
        v
02 Payment funding foundation
        |
        +-------------------+
        v                   v
03 Deal lifecycle       04 Fraud boundary
        |                   |
        +---------+---------+
                  v
05 Conditional settlement and disputes
                  |
                  v
06 Omnichannel notifications
                  |
                  v
07 Reconciliation and operational hardening
```

## Plans

- [Service boundaries](service-boundaries.md)
- [User Service](user-service/README.md)
- [Deal Service](deal-service/README.md)
- [Payment Service](payment-service/README.md)
- [Fraud Engine](fraud-engine/README.md)
- [Notification Service](notification-service/README.md)
- [Reconciliation Service](reconciliation-service/README.md)
- [API Gateway](api-gateway/README.md)

## Delivery rule

Each numbered section in a service plan is intended to become a separate issue,
branch and reviewable pull request. Cross-service contracts are versioned and
documented before the producer and consumer implementations are coupled to them.

