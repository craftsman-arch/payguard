# Reconciliation Service build plan

## Goal

Detect and report differences between PayGuard financial records and payment
provider truth without reading another service's database or silently moving
money.

## 01 - Reconciliation contracts

- Consume or request scoped facts for Payment, Settlement, Refund and Reversal.
- Compare internal amounts/statuses with Stripe Charge, PaymentIntent, Transfer,
  Refund and reversal objects.
- Correlate through stable internal/provider references.

## 02 - Discrepancy workflow

- Detect missing webhooks, unknown outcomes, amount mismatches, duplicate
  operations and stale pending states.
- Persist discrepancy cases with severity and audit history.
- Request controlled repair from the owning service instead of updating its DB.
- Keep unresolved financial cases operationally visible.

## 03 - Scheduled and operational execution

- Support incremental schedules, backfill windows and provider rate limits.
- Make jobs restartable and idempotent.
- Add reconciliation lag, discrepancy and repair metrics.
- Document manual investigation and escalation runbooks.

