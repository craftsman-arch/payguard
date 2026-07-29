package io.payguard.userservice.integration.persistence.entity;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.merchant.MerchantStatus;
import io.payguard.userservice.domain.merchant.PaymentAccountRequiredAction;
import io.payguard.userservice.domain.merchant.PaymentAccountStatus;
import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.merchant.value.Email;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private Email email;

    @Column(nullable = false)
    private String legalName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(nullable = false, length = 2)
    private Country country;

    @Column(unique = true)
    private String identityUserId;

    @Column(unique = true)
    private String paymentAccountId;

    @Column
    private Instant paymentAccountCreationStartedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentAccountStatus paymentAccountStatus;

    @Column(length = 255)
    private String paymentAccountStatusReason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentAccountRequiredAction paymentAccountRequiredAction;

    @Column
    private Instant lastPaymentAccountEventAt;

    @Column(nullable = false)
    private boolean cardPaymentsCapabilityActive;

    @Column(nullable = false)
    private boolean transfersCapabilityActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MerchantStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long revision;

}
