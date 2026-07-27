package io.payguard.userservice.integration.persistence.repository;

import io.payguard.userservice.integration.persistence.entity.ProcessedStripeEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ProcessedStripeEventJpaRepository extends JpaRepository<ProcessedStripeEventEntity, String> {

    @Modifying
    @Query(
            value = """
                    INSERT INTO processed_stripe_events (
                        event_id,
                        event_type,
                        processed_at
                    )
                    VALUES (
                        :eventId,
                        :eventType,
                        :processedAt
                    )
                    ON CONFLICT (event_id) DO NOTHING
                    """,
            nativeQuery = true
    )
    int tryInsert(
            @Param("eventId") String eventId,
            @Param("eventType") String eventType,
            @Param("processedAt") Instant processedAt
    );
}