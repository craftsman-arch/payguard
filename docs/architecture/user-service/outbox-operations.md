# Transactional outbox operations

## Delivery model

User Service records integration events in `outbox_events` in the same
PostgreSQL transaction as the corresponding Merchant state change.

Kafka publication is at least once:

```text
business transaction
→ outbox insert
→ commit
→ claim bounded batch
→ Kafka send and acknowledgement
→ mark published
```

Consumers must deduplicate by the immutable `eventId`.

`aggregateId` is the Kafka record key. Events for one aggregate therefore use
one Kafka partition. `sequence_number` defines the local outbox order before
publication.

## Claims and ordering

The publisher claims events using:

```sql
FOR UPDATE SKIP LOCKED
```

A claim contains a unique `claim_id` and a database-time lease in
`claimed_until`. Publication confirmation and failure handling require both
the event ID and the currently owned claim ID.

The earliest unpublished event is the head of its aggregate:

- a later event cannot bypass an unpublished predecessor;
- an exhausted head blocks only its own aggregate;
- events for other aggregates remain eligible;
- an expired claim can be reclaimed by another publisher;
- a publisher that lost its claim cannot modify the event afterward.

Kafka I/O is outside database transactions. Short `REQUIRES_NEW` transactions
claim, confirm, reschedule or exhaust events.

## Automatic retries

A confirmed publication failure:

1. increments `attempts`;
2. stores a sanitized `last_error`;
3. clears the current claim;
4. schedules `next_attempt_at` using bounded backoff.

After the configured maximum number of attempts, `failed_at` is set. The event
then requires operator recovery and continues blocking later events of the same
aggregate.

## Expired claims

Expired claims require no manual operation. The next publisher atomically
replaces the expired claim and continues normal publication.

Recovery is recorded through:

```text
outbox_publication_stale_claim_recovered_total
```

The warning log contains the event ID, old and new claim IDs, aggregate
identity and correlation ID. It never contains the event payload.

## Manual exhausted-event recovery

Recovery uses the original outbox row. It does not publish directly to Kafka
and does not create a replacement event.

Administrative endpoint:

```http
POST /api/v1/admin/outbox/events/{eventId}/recovery
Authorization: Bearer <ADMIN token>
Content-Type: application/json

{
  "reason": "Kafka configuration was corrected."
}
```

The endpoint is intended for the internal operational boundary and is not
routed through the public API Gateway.

Successful recovery returns `202 Accepted`. User Service:

1. locks the outbox row;
2. verifies that it is unpublished and exhausted;
3. records the previous failure in `outbox_event_recoveries`;
4. clears the terminal failure and claim;
5. resets `attempts`;
6. schedules the original event for immediate publication.

The event ID, payload, event type, aggregate identity and sequence number do
not change.

Possible responses:

| Status | Meaning |
|---|---|
| `202` | Original event returned to the publication pipeline |
| `401` | Authentication missing or invalid |
| `403` | Authenticated principal does not have `ADMIN` |
| `404` | Event does not exist |
| `409` | Event is not exhausted or is already published |

Before recovery, investigate `last_error`, Kafka availability and publisher
logs. After recovery, verify:

```sql
SELECT
    id,
    attempts,
    published_at,
    failed_at,
    claim_id,
    last_error
FROM outbox_events
WHERE id = :eventId;
```

and the audit record:

```sql
SELECT *
FROM outbox_event_recoveries
WHERE event_id = :eventId
ORDER BY recovered_at DESC;
```

## Metrics

Database-state gauges are refreshed by one scheduled query and cached in
memory. Prometheus scrapes never query PostgreSQL.

| Prometheus metric | Meaning |
|---|---|
| `outbox_events_pending` | Unpublished events that are not exhausted |
| `outbox_events_exhausted` | Unpublished exhausted events |
| `outbox_aggregates_blocked` | Aggregates blocked by an exhausted event |
| `outbox_event_oldest_pending_age_seconds` | Age of the oldest pending event |
| `outbox_event_oldest_exhausted_age_seconds` | Age of the oldest exhausted event |
| `outbox_observability_refresh_errors_total` | Failed database snapshot refreshes |
| `outbox_publication_published_total` | Successfully published events |
| `outbox_publication_attempt_failed_total` | Failures durably recorded in outbox state |
| `outbox_publication_retry_total` | Events scheduled for another attempt |
| `outbox_publication_failed_total` | Events that exhausted all attempts |
| `outbox_publication_lost_claim_total` | Outcomes rejected after claim ownership was lost |
| `outbox_publication_stale_claim_recovered_total` | Expired claims reclaimed |
| `outbox_recovery_completed_total` | Successful manual recoveries |

Every User Service replica reads the same PostgreSQL state. Aggregate snapshot
gauges across replicas with `max`, not `sum`. Publication counters belong to
individual replicas and are normally queried by summing their rates.

No event ID, aggregate ID, email or other high-cardinality value is used as a
metric label.

## Retention and housekeeping

Operational tables are bounded storage, not permanent business audit logs.

| Storage | Default retention | Deletion condition |
|---|---:|---|
| `processed_stripe_events` | 45 days | `processed_at` is older than cutoff |
| Published `outbox_events` | 30 days | `published_at` is set and older than cutoff |
| `outbox_event_recoveries` | 90 days | `recovered_at` is older than cutoff |

Pending, retrying, claimed and exhausted unpublished outbox events are never
eligible for retention cleanup.

Configuration:

```yaml
housekeeping:
  retention:
    enabled: true
    processed-stripe-events: 45d
    published-outbox-events: 30d
    outbox-event-recoveries: 90d
    batch-size: 1000
    cron: "0 0 3 * * *"
    zone: UTC
```

Each scheduled execution deletes at most one bounded batch from each storage
type. Every batch uses its retention index, a short `REQUIRES_NEW` transaction
and `FOR UPDATE SKIP LOCKED`.

ShedLock is intentionally not used. Concurrent replicas safely select
different row batches through PostgreSQL row-level locking. Cleanup is
idempotent, performs no external side effect and does not require a single
global executor.

Housekeeping metrics:

```text
housekeeping_retention_executions_total
housekeeping_retention_deleted_total
housekeeping_retention_failures_total
housekeeping_retention_duration_seconds
```

The `storage` label has exactly three bounded values:

```text
processed_stripe_events
published_outbox_events
outbox_event_recoveries
```

Logs and metrics report counts and operational identifiers but never event
payloads, passwords, identity action links, onboarding links or KYC data.
