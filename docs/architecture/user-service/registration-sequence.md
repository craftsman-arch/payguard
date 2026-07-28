# Merchant registration and onboarding

```mermaid
sequenceDiagram
    actor Client as Merchant client
    participant API as API Gateway
    participant Registration as User Service
    participant DB as PostgreSQL
    participant Keycloak
    participant Stripe

    Client->>API: POST /api/merchants
    API->>Registration: POST /api/v1/merchants

    Registration->>DB: Find Merchant by email
    alt Merchant does not exist
        Registration->>DB: Insert Merchant (PENDING, revision 0)
        DB-->>Registration: Commit
    else Merchant already exists
        DB-->>Registration: Existing Merchant
    end

    opt Identity is not linked
        Registration->>Keycloak: Create user
        Keycloak-->>Registration: identityUserId
        Registration->>Keycloak: Assign MERCHANT role
    end

    opt Payment account is not linked
        Registration->>Stripe: Create connected account
        Stripe-->>Registration: paymentAccountId
    end

    Registration->>DB: Save identity/payment links with revision check
    Registration->>Stripe: Create account onboarding link
    Stripe-->>Registration: onboardingUrl
    Registration-->>API: id, PENDING, onboardingUrl
    API-->>Client: Registration response

    Client->>Stripe: Complete hosted onboarding
    Stripe-->>API: account.updated webhooks
    API-->>Registration: Signed webhook
    Registration->>DB: Update normalized state and activate when ready
```

## Properties

- Registration is idempotent by merchant email at the application boundary.
- Merchant remains `PENDING` until Stripe webhook state satisfies activation
  policy.
- Stripe account links are short-lived; an eligible authenticated merchant can
  request a fresh link through
  `POST /api/merchants/me/payment-account/onboarding-link`.
- Keycloak passwords, action links and Stripe onboarding links are never
  published to Kafka.
- Current external provisioning compensation is best effort; durable recovery
  is handled by the reliability plan.
