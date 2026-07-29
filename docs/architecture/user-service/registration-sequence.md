# Merchant registration and onboarding

```mermaid
sequenceDiagram
    actor Client as Merchant client
    participant API as API Gateway
    participant Registration as User Service
    participant DB as PostgreSQL
    participant Keycloak
    participant Email as Email provider
    participant Stripe

    Client->>API: POST /api/merchants (email, password, business data)
    API->>Registration: POST /api/v1/merchants

    Registration->>DB: Find Merchant by email
    alt Merchant does not exist
        Registration->>DB: Insert Merchant (PENDING, revision 0)
        DB-->>Registration: Commit
    else Merchant already exists
        Registration-->>API: 409 Conflict
        API-->>Client: Merchant already exists
    end

    Registration->>Keycloak: Create enabled user with permanent password
    Note over Registration,Keycloak: emailVerified=false; VERIFY_EMAIL and CONFIGURE_TOTP
    Keycloak-->>Registration: identityUserId
    Registration->>Keycloak: Assign MERCHANT role

    Registration->>Stripe: Create connected account
    Stripe-->>Registration: paymentAccountId

    Registration->>DB: Save identity/payment links with revision check
    Registration->>Stripe: Create account onboarding link
    Stripe-->>Registration: onboardingUrl
    Registration-->>API: id, PENDING, onboardingUrl
    API-->>Client: Registration response

    Client->>Keycloak: Authorization Code + PKCE login
    Keycloak->>Email: Send email verification link
    Email-->>Client: Verification email
    Client->>Keycloak: Confirm email
    Keycloak-->>Client: Require OTP configuration
    Client->>Keycloak: Configure OTP and finish login

    Client->>Stripe: Complete hosted onboarding
    Stripe-->>API: account.updated webhooks
    API-->>Registration: Signed webhook
    Registration->>DB: Update normalized state and activate when ready
```

## Properties

- Merchant registration does not use an invitation. The merchant selects a
  password in the PayGuard registration form.
- User Service passes the password to Keycloak as a permanent credential. The
  password is not part of the Merchant aggregate and is not returned by the
  registration endpoint.
- Keycloak enforces the realm password policy, email verification and OTP.
- A duplicate Merchant or Keycloak identity produces a conflict; existing
  identities are not automatically linked to a new registration attempt.
- Registration does not authenticate the merchant. Authentication happens
  through Authorization Code with PKCE after registration.
- Merchant remains `PENDING` until Stripe webhook state satisfies activation
  policy.
- Stripe account links are short-lived; an eligible authenticated merchant can
  request a fresh link through
  `POST /api/merchants/me/payment-account/onboarding-link`.
- Keycloak passwords, action links and Stripe onboarding links are never
  published to Kafka.
- Current external provisioning compensation is best effort; durable recovery
  and retry/concurrency handling are covered by the reliability plan.
