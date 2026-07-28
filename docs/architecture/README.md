# PayGuard architecture

This directory describes the implemented architecture of PayGuard services.
Planned work belongs in the rebuild-plan documents rather than here.

## User Service

- [Application architecture](user-service/application-architecture.md)
- [Merchant lifecycle](user-service/merchant-lifecycle.md)
- [Merchant registration sequence](user-service/registration-sequence.md)
- [Stripe webhook processing](user-service/webhook-processing-sequence.md)
- [Merchant payment status Kafka contract](../contracts/kafka/merchant-payment-account-status-changed-v1.md)

## Other services

API Gateway currently provides the external routing and authentication boundary.
Payment, Fraud, Notification and Reconciliation service architecture will be
documented when their implementations enter scope.
