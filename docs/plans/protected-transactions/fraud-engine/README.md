# Fraud Engine build plan

## Goal

Provide explainable, versioned risk decisions for deal funding and settlement
release without owning payment or deal state.

## 01 - Assessment contract

- Define assessment purposes `PAYMENT_INITIATION` and `SETTLEMENT_RELEASE`.
- Return `ALLOW`, `REVIEW` or `BLOCK`, risk score, assessment ID, model version,
  feature-set version and bounded reason codes.
- Reject raw card data and unbounded PII.
- Make repeated assessment requests idempotent.

## 02 - Rules and feature pipeline

- Build bounded features from trusted payment/deal context and retained risk
  history.
- Add deterministic velocity, amount, account-age, relationship and dispute
  rules.
- Cache appropriate short-lived features in Redis without making Redis the
  system of record.
- Preserve contributing factors for audit.

## 03 - Trained model

- Train an interpretable model on a documented synthetic dataset.
- Export and execute the model through ONNX.
- Version the model artifact, feature schema and decision thresholds.
- Keep deterministic rules as a controlled fallback.
- Add model-quality and inference regression tests.

## 04 - Review integration and operations

- Expose decisions to the authorized risk-review workflow.
- Add latency, decision-distribution, fallback and model-version metrics.
- Detect drift and threshold changes without high-cardinality labels.
- Document degraded behavior and rollback of a model version.

