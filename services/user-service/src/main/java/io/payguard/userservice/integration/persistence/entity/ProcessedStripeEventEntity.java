package io.payguard.userservice.integration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "processed_stripe_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProcessedStripeEventEntity {

    @Id
    @Column(
            name = "event_id",
            nullable = false,
            updatable = false,
            length = 255
    )
    private String eventId;

    @Column(
            name = "event_type",
            nullable = false,
            updatable = false,
            length = 64
    )
    private String eventType;

    @Column(
            name = "processed_at",
            nullable = false,
            updatable = false
    )
    private Instant processedAt;
}