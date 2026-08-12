package io.payguard.userservice.domain.merchant;

import io.payguard.userservice.domain.merchant.exception.*;
import io.payguard.userservice.domain.merchant.event.MerchantPaymentStateChanged;
import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.common.value.Email;
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
    private final long revision;

    private MerchantStatus status;
    private String identityUserId;
    private String paymentAccountId;
    private Instant paymentAccountCreationStartedAt;
    private PaymentAccountStatus paymentAccountStatus;
    private String paymentAccountStatusReason;
    private PaymentAccountRequiredAction paymentAccountRequiredAction;
    private Instant lastPaymentAccountEventAt;
    private boolean cardPaymentsCapabilityActive;
    private boolean transfersCapabilityActive;
    private final Instant createdAt;
    private Instant updatedAt;

    private Merchant(
            UUID id,
            Email email,
            String legalName,
            BusinessType businessType,
            Country country,
            long revision,
            String identityUserId,
            String paymentAccountId,
            Instant paymentAccountCreationStartedAt,
            PaymentAccountStatus paymentAccountStatus,
            String paymentAccountStatusReason,
            PaymentAccountRequiredAction paymentAccountRequiredAction,
            Instant lastPaymentAccountEventAt,
            boolean cardPaymentsCapabilityActive,
            boolean transfersCapabilityActive,
            MerchantStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.legalName = legalName;
        this.businessType = businessType;
        this.country = country;
        this.revision = revision;
        this.identityUserId = identityUserId;
        this.paymentAccountId = paymentAccountId;
        this.paymentAccountCreationStartedAt =
                paymentAccountCreationStartedAt;
        this.paymentAccountStatus = paymentAccountStatus;
        this.paymentAccountStatusReason = paymentAccountStatusReason;
        this.paymentAccountRequiredAction = paymentAccountRequiredAction;
        this.lastPaymentAccountEventAt = lastPaymentAccountEventAt;
        this.cardPaymentsCapabilityActive = cardPaymentsCapabilityActive;
        this.transfersCapabilityActive = transfersCapabilityActive;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Merchant registerForIdentity(
            @NonNull UUID id,
            @NonNull Email email,
            @NonNull String legalName,
            @NonNull BusinessType businessType,
            @NonNull Country country,
            @NonNull String identityUserId,
            @NonNull Instant now
    ) {

        return new Merchant(
                id,
                email,
                legalName,
                businessType,
                country,
                0,
                identityUserId,
                null,
                null,
                PaymentAccountStatus.PENDING_ONBOARDING,
                null,
                PaymentAccountRequiredAction.CONTINUE_ONBOARDING,
                null,
                false,
                false,
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
            long revision,
            String identityUserId,
            String paymentAccountId,
            Instant paymentAccountCreationStartedAt,
            @NonNull PaymentAccountStatus paymentAccountStatus,
            String paymentAccountStatusReason,
            @NonNull PaymentAccountRequiredAction paymentAccountRequiredAction,
            Instant lastPaymentAccountEventAt,
            boolean cardPaymentsCapabilityActive,
            boolean transfersCapabilityActive,
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
                revision,
                identityUserId,
                paymentAccountId,
                paymentAccountCreationStartedAt,
                paymentAccountStatus,
                paymentAccountStatusReason,
                paymentAccountRequiredAction,
                lastPaymentAccountEventAt,
                cardPaymentsCapabilityActive,
                transfersCapabilityActive,
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

    public boolean canProvisionPaymentAccount() {
        return isPending() && !hasPaymentAccount();
    }

    public boolean isReadyForPayments() {
        return isActive() && paymentAccountStatus.isActive();
    }

    public boolean isEligibleForDestinationCharges() {
        return isReadyForPayments()
                && transfersCapabilityActive;
    }

    public MerchantPaymentState paymentState() {

        return new MerchantPaymentState(
                paymentAccountStatus,
                paymentAccountRequiredAction,
                isReadyForPayments(),
                isEligibleForDestinationCharges()
        );
    }

    public boolean canRequestPaymentAccountOnboardingLink() {

        return hasPaymentAccount()
                && !isSuspended()
                && !paymentAccountStatus.isDisabled()
                && paymentAccountRequiredAction.requiresOnboarding();
    }

    public void linkPaymentProvider(String paymentAccountId, Instant now) {

        if (!isPending()) {
            throw new MerchantNotReadyForActivationException();
        }

        if (hasPaymentAccount()) {
            throw new MerchantAlreadyLinkedToPaymentProviderException();
        }

        this.paymentAccountId = paymentAccountId;
        this.paymentAccountCreationStartedAt = null;
        this.paymentAccountStatus = PaymentAccountStatus.PENDING_ONBOARDING;
        this.paymentAccountRequiredAction =
                PaymentAccountRequiredAction.CONTINUE_ONBOARDING;
        this.updatedAt = now;
    }

    public void recordPaymentAccountCreationStarted(Instant now) {

        if (hasPaymentAccount()) {
            throw new MerchantAlreadyLinkedToPaymentProviderException();
        }

        if (paymentAccountCreationStartedAt == null) {
            this.paymentAccountCreationStartedAt = now;
            this.updatedAt = now;
        }
    }

    public void clearPaymentAccountCreationAttempt(Instant now) {

        if (hasPaymentAccount()) {
            return;
        }

        this.paymentAccountCreationStartedAt = null;
        this.updatedAt = now;
    }

    public PaymentAccountUpdateResult recordPaymentAccountUpdate(
            boolean payoutsEnabled,
            boolean cardPaymentsCapabilityActive,
            boolean transfersCapabilityActive,
            String disabledReason,
            @NonNull PaymentAccountRequirements requirements,
            Instant eventAt,
            Instant now
    ) {

        if (lastPaymentAccountEventAt != null
                && eventAt.isBefore(lastPaymentAccountEventAt)) {

            return new PaymentAccountUpdateResult.Stale();
        }

        MerchantPaymentState previousState =
                paymentState();

        this.paymentAccountStatus = resolvePaymentAccountStatus(
                payoutsEnabled,
                disabledReason
        );

        this.paymentAccountRequiredAction =
                PaymentAccountRequiredAction.resolve(
                        paymentAccountStatus,
                        requirements
                );

        this.paymentAccountStatusReason = disabledReason;
        this.cardPaymentsCapabilityActive = cardPaymentsCapabilityActive;
        this.transfersCapabilityActive = transfersCapabilityActive;
        this.lastPaymentAccountEventAt = eventAt;
        this.updatedAt = now;

        if (paymentAccountStatus.isActive() && isPending()) {
            activate(now);
        }

        MerchantPaymentState currentState =
                paymentState();

        if (!currentState.hasChangedSince(previousState)) {
            return new PaymentAccountUpdateResult
                    .AppliedWithoutStateChange();
        }

        return new PaymentAccountUpdateResult.StateChanged(
                new MerchantPaymentStateChanged(
                        id,
                        previousState,
                        currentState,
                        now
                )
        );
    }

    private PaymentAccountStatus resolvePaymentAccountStatus(
            boolean payoutsEnabled,
            String disabledReason
    ) {

        if (!hasPaymentAccountRestriction(disabledReason) && payoutsEnabled) {
            return PaymentAccountStatus.ACTIVE;
        }

        return isPending()
                ? PaymentAccountStatus.PENDING_ONBOARDING
                : PaymentAccountStatus.RESTRICTED;
    }

    private boolean hasPaymentAccountRestriction(String reason) {
        return reason != null && !reason.isBlank();
    }

    public void activate(Instant now) {

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

    public void suspend(Instant now) {

        if (!isActive()) {
            throw new MerchantMustBeActiveException();
        }

        this.status = MerchantStatus.SUSPENDED;
        this.updatedAt = now;
    }

}
