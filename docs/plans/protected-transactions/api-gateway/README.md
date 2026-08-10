# API Gateway evolution

## Goal

Expose the protected-transaction platform through explicit public routes while
preserving authentication, abuse protection and service boundaries.

## Scope

- Replace merchant-only registration/profile routes with user routes when User
  Service migration is complete.
- Add exact public/user-authenticated Deal Service routes.
- Keep Payment Service funding, release and reconciliation endpoints internal.
- Preserve exact Stripe webhook routing with no JWT and mandatory downstream
  signature verification.
- Apply distributed rate limits and request-size limits to registration,
  authentication-adjacent and deal-creation operations.
- Define trusted proxy handling, narrow CORS and stable `429`/`503` responses.
- Never log registration passwords, Checkout URLs, authorization headers or
  request bodies containing deal evidence.

## Acceptance boundary

Gateway authentication is not business authorization. User, Deal and Payment
services independently verify participant ownership, lifecycle state and
service scopes.

