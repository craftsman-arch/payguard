package io.payguard.userservice.integration.persistence.repository;

import io.payguard.userservice.domain.settlement.SettlementAccountRequiredAction;
import io.payguard.userservice.domain.settlement.SettlementAccountStatus;
import io.payguard.userservice.domain.settlement.SettlementProvider;
import io.payguard.userservice.integration.persistence.entity.SettlementAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SettlementAccountJpaRepository extends JpaRepository<SettlementAccountEntity, UUID> {

    Optional<SettlementAccountEntity> findByUserId(UUID userId);

    Optional<SettlementAccountEntity> findByProviderAndProviderAccountId(SettlementProvider provider, String providerAccountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SettlementAccountEntity acc
               SET acc.providerAccountId = :providerAccountId,
                   acc.creationStartedAt = :creationStartedAt,
                   acc.status = :status,
                   acc.statusReason = :statusReason,
                   acc.requiredAction = :requiredAction,
                   acc.lastProviderEventAt = :lastProviderEventAt,
                   acc.transfersCapabilityActive = :transfersCapabilityActive,
                   acc.updatedAt = :updatedAt,
                   acc.revision = acc.revision + 1
             WHERE acc.id = :id
               AND acc.revision = :expectedRevision
            """)
    int updateIfRevisionMatches(
            @Param("id") UUID id,
            @Param("providerAccountId") String providerAccountId,
            @Param("creationStartedAt") Instant creationStartedAt,
            @Param("status") SettlementAccountStatus status,
            @Param("statusReason") String statusReason,
            @Param("requiredAction") SettlementAccountRequiredAction requiredAction,
            @Param("lastProviderEventAt") Instant lastProviderEventAt,
            @Param("transfersCapabilityActive") boolean transfersCapabilityActive,
            @Param("updatedAt") Instant updatedAt,
            @Param("expectedRevision") long expectedRevision
    );
}
