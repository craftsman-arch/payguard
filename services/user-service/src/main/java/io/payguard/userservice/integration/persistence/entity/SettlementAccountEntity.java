package io.payguard.userservice.integration.persistence.entity;

import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.payguard.userservice.domain.settlement.SettlementAccountRequiredAction;
import io.payguard.userservice.domain.settlement.SettlementAccountStatus;
import io.payguard.userservice.domain.settlement.SettlementProvider;
import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlement_accounts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementAccountEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private SettlementProvider provider;

    @Column
    private String providerAccountId;

    @Column(nullable = false, updatable = false)
    private AccountHolderName accountHolderName;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private SettlementAccountHolderType accountHolderType;

    @Column(nullable = false, length = 2, updatable = false)
    private Country country;

    @Column
    private Instant creationStartedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementAccountStatus status;

    @Column(length = 255)
    private String statusReason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementAccountRequiredAction requiredAction;

    @Column
    private Instant lastProviderEventAt;

    @Column(nullable = false)
    private boolean transfersCapabilityActive;

    @Column(nullable = false)
    private long revision;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
