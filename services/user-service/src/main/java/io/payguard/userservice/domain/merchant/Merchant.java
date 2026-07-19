package io.payguard.userservice.domain.merchant;

import io.payguard.userservice.domain.merchant.exception.*;
import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.merchant.value.Email;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Merchant {

    private final UUID id;
    private final Email email;
    private final Country country;
    private final String legalName;
    private final BusinessType businessType;

    private MerchantStatus status;
    private String identityUserId;
    private String paymentAccountId;

    private final Instant createdAt;
    private Instant updatedAt;

    private Merchant(
            UUID id,
            Email email,
            String legalName,
            BusinessType businessType,
            Country country,
            String identityUserId,
            String paymentAccountId,
            MerchantStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.legalName = legalName;
        this.businessType = businessType;
        this.country = country;
        this.identityUserId = identityUserId;
        this.paymentAccountId = paymentAccountId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Merchant register(
            @NonNull UUID id,
            @NonNull Email email,
            @NonNull String legalName,
            @NonNull BusinessType businessType,
            @NonNull Country country,
            @NonNull Instant now
    ) {

        return new Merchant(
                id,
                email,
                legalName,
                businessType,
                country,
                null,
                null,
                MerchantStatus.PENDING,
                now,
                now
        );
    }

    public static Merchant restore(
            @NonNull UUID id,
            @NonNull Email email,
            @NonNull String legalName,
            @NonNull BusinessType businessType,
            @NonNull Country country,
            String identityUserId,
            String paymentAccountId,
            @NonNull MerchantStatus status,
            @NonNull Instant createdAt,
            @NonNull Instant updatedAt
    ) {

        return new Merchant(
                id,
                email,
                legalName,
                businessType,
                country,
                identityUserId,
                paymentAccountId,
                status,
                createdAt,
                updatedAt
        );
    }

    public boolean isPending() {
        return status == MerchantStatus.PENDING;
    }

    public boolean isActive() {
        return status == MerchantStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == MerchantStatus.SUSPENDED;
    }

    public boolean hasIdentity() {
        return identityUserId != null;
    }

    public boolean hasPaymentAccount() {
        return paymentAccountId != null;
    }

    public boolean canProvisionIdentity() {
        return isPending() && !hasIdentity();
    }

    public boolean canProvisionPaymentAccount() {
        return isPending() && !hasPaymentAccount();
    }

    public void linkIdentity(
            String identityUserId,
            Instant now
    ) {

        if (!isPending()) {
            throw new MerchantNotReadyForActivationException();
        }

        if (hasIdentity()) {
            throw new MerchantAlreadyLinkedToIdentityProviderException();
        }

        this.identityUserId = identityUserId;
        this.updatedAt = now;
    }

    public void linkPaymentProvider(
            String paymentAccountId,
            Instant now
    ) {

        if (!isPending()) {
            throw new MerchantNotReadyForActivationException();
        }

        if (hasPaymentAccount()) {
            throw new MerchantAlreadyLinkedToPaymentProviderException();
        }

        this.paymentAccountId = paymentAccountId;
        this.updatedAt = now;
    }

    public void activate(
            Instant now
    ) {

        if (isActive()) {
            throw new MerchantAlreadyActivatedException();
        }

        if (!isPending()) {
            throw new MerchantNotReadyForActivationException();
        }

        if (!hasIdentity()) {
            throw new MerchantIdentityNotLinkedException();
        }

        if (!hasPaymentAccount()) {
            throw new MerchantPaymentAccountNotLinkedException();
        }

        this.status = MerchantStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void suspend(
            Instant now
    ) {

        if (!isActive()) {
            throw new MerchantMustBeActiveException();
        }

        this.status = MerchantStatus.SUSPENDED;
        this.updatedAt = now;
    }

}