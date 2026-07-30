package io.payguard.userservice.integration.housekeeping;

enum OperationalDataRetentionTarget {

    PROCESSED_STRIPE_EVENTS("processed_stripe_events"),
    PUBLISHED_OUTBOX_EVENTS("published_outbox_events"),
    OUTBOX_EVENT_RECOVERIES("outbox_event_recoveries");

    private final String metricTag;

    OperationalDataRetentionTarget(String metricTag) {
        this.metricTag = metricTag;
    }

    String metricTag() {
        return metricTag;
    }
}
