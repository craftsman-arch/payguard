# Merchant registration and onboarding

```mermaid
sequenceDiagram
    actor Client as Merchant portal
    participant API as API Gateway
    participant UserService as User Service
    participant DB as PostgreSQL
    participant Keycloak
    participant Email as Email provider
    participant Stripe

    Client->>API: POST /api/merchant-registrations (email, password)
    API->>UserService: POST /api/v1/merchant-registrations
    UserService->>Keycloak: Create enabled identity with permanent password
    Note over UserService,Keycloak: MERCHANT; emailVerified=false; VERIFY_EMAIL; CONFIGURE_TOTP
    UserService->>Keycloak: Request verification email
    Keycloak->>Email: Send verification link
    UserService-->>Client: 202 PENDING_VERIFICATION

    Email-->>Client: Verification email
    Client->>Keycloak: Confirm email
    Client->>Keycloak: Authorization Code + PKCE login
    Keycloak-->>Client: Require OTP configuration
    Client->>Keycloak: Configure OTP and finish login

    Client->>API: POST /api/merchants (business data + JWT)
    API->>UserService: Authenticated Merchant profile request
    UserService->>UserService: Derive identityUserId and email from JWT
    UserService->>DB: Create or load Merchant
    DB-->>UserService: Merchant (PENDING)
    UserService-->>Client: 201 created or 200 existing

    Client->>API: POST /api/merchants/me/payment-account/onboarding
    API->>UserService: Authenticated onboarding request
    alt Merchant has no Stripe account
        UserService->>DB: Mark account creation started
        UserService->>Stripe: Create account with stable idempotency key
        Stripe-->>UserService: paymentAccountId
        UserService->>DB: Persist paymentAccountId
    else Stripe account already linked
        UserService->>DB: Load existing paymentAccountId
    end
    UserService->>Stripe: Create fresh Account Link
    Stripe-->>UserService: onboardingUrl
    UserService-->>Client: onboardingUrl

    Client->>Stripe: Complete hosted onboarding
    Stripe-->>API: account.updated webhooks
    API->>UserService: Signed webhook
    UserService->>DB: Update normalized state and activate when ready
```

## Properties

- Merchant registration does not use an invitation.
- Identity registration accepts only email and the merchant-selected password.
- The password is forwarded to Keycloak as a permanent credential and is never
  stored in Merchant persistence, events or API responses.
- Keycloak owns password policy, email verification and OTP credentials.
- Identity registration creates no Merchant or Stripe resource and issues no
  authentication token.
- Verification-email recovery is idempotent and uses the managed
  `payguard.internal.identity_origin` marker to avoid converting staff
  identities into merchants.
- Merchant profile creation requires an authenticated `MERCHANT` with verified
  email. Identity ID and email are derived from trusted JWT claims.
- Profile creation is idempotent for the authenticated identity.
- Stripe onboarding is a separate authenticated stage.
- Stripe account creation uses a stable Merchant-based idempotency key.
- A linked Stripe account is reused and a fresh short-lived Account Link is
  created when necessary.
- A valid identity, a Merchant without a Stripe account and incomplete Stripe
  onboarding are legitimate recoverable states.
- Stripe failure never causes automatic deletion of a valid Keycloak identity
  or Merchant.
- Merchant remains `PENDING` until webhook state satisfies activation policy.
- Passwords, identity action links and Stripe onboarding links are never
  published to Kafka.
