package io.payguard.userservice.domain.settlement;

import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.settlement.event.SettlementAccountStateChanged;
import io.payguard.userservice.domain.settlement.exception.InvalidSettlementAccountRevisionException;
import io.payguard.userservice.domain.settlement.exception.SettlementAccountAlreadyLinkedException;
import io.payguard.userservice.domain.settlement.exception.SettlementAccountNotLinkedException;
import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.value.UserId;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

@Getter
public class SettlementAccount {

    private final SettlementAccountId id;
    private final UserId userId;
    private final SettlementProvider provider;
    private final AccountHolderName accountHolderName;
    private final SettlementAccountHolderType accountHolderType;
    private final Country country;
    private final long revision;
    private final Instant createdAt;

    private String providerAccountId;
    private Instant creationStartedAt;
    private SettlementAccountStatus status;
    private String statusReason;
    private SettlementAccountRequiredAction requiredAction;
    private Instant lastProviderEventAt;
    private boolean transfersCapabilityActive;
    private Instant updatedAt;

    private SettlementAccount(
            SettlementAccountId id,
            UserId userId,
            SettlementProvider provider,
            AccountHolderName accountHolderName,
            SettlementAccountHolderType accountHolderType,
            Country country,
            String providerAccountId,
            Instant creationStartedAt,
            SettlementAccountStatus status,
            String statusReason,
            SettlementAccountRequiredAction requiredAction,
            Instant lastProviderEventAt,
            boolean transfersCapabilityActive,
            long revision,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (revision < 0) {
            throw new InvalidSettlementAccountRevisionException(revision);
        }

        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.accountHolderName = accountHolderName;
        this.accountHolderType = accountHolderType;
        this.country = country;
        this.providerAccountId = providerAccountId;
        this.creationStartedAt = creationStartedAt;
        this.status = status;
        this.statusReason = statusReason;
        this.requiredAction = requiredAction;
        this.lastProviderEventAt = lastProviderEventAt;
        this.transfersCapabilityActive = transfersCapabilityActive;
        this.revision = revision;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SettlementAccount open(
            @NonNull SettlementAccountId id,
            @NonNull UserId userId,
            @NonNull SettlementProvider provider,
            @NonNull AccountHolderName accountHolderName,
            @NonNull SettlementAccountHolderType accountHolderType,
            @NonNull Country country,
            @NonNull Instant now
    ) {
        return new SettlementAccount(
                id,
                userId,
                provider,
                accountHolderName,
                accountHolderType,
                country,
                null,
                null,
                SettlementAccountStatus.PENDING_ONBOARDING,
                null,
                SettlementAccountRequiredAction.CONTINUE_ONBOARDING,
                null,
                false,
                0,
                now,
                now
        );
    }

    public static SettlementAccount restore(
            @NonNull SettlementAccountId id,
            @NonNull UserId userId,
            @NonNull SettlementProvider provider,
            @NonNull AccountHolderName accountHolderName,
            @NonNull SettlementAccountHolderType accountHolderType,
            @NonNull Country country,
            String providerAccountId,
            Instant creationStartedAt,
            @NonNull SettlementAccountStatus status,
            String statusReason,
            @NonNull SettlementAccountRequiredAction requiredAction,
            Instant lastProviderEventAt,
            boolean transfersCapabilityActive,
            long revision,
            @NonNull Instant createdAt,
            @NonNull Instant updatedAt
    ) {
        return new SettlementAccount(
                id, userId, provider, accountHolderName, accountHolderType,
                country, providerAccountId, creationStartedAt, status,
                statusReason, requiredAction, lastProviderEventAt,
                transfersCapabilityActive, revision, createdAt, updatedAt
        );
    }

    public boolean hasProviderAccount() {
        return providerAccountId != null;
    }

    public boolean canProvisionProviderAccount() {
        return !hasProviderAccount();
    }

    public boolean canReceiveTransfers() {
        return status.isActive() && transfersCapabilityActive;
    }

    public SettlementAccountState state() {
        return new SettlementAccountState(
                status,
                requiredAction,
                canReceiveTransfers()
        );
    }

    public void recordCreationStarted(@NonNull Instant now) {
        if (hasProviderAccount()) {
            throw new SettlementAccountAlreadyLinkedException();
        }

        if (creationStartedAt == null) {
            creationStartedAt = now;
            updatedAt = now;
        }
    }

    public void clearCreationAttempt(@NonNull Instant now) {
        if (hasProviderAccount()) {
            return;
        }

        creationStartedAt = null;
        updatedAt = now;
    }

    public void linkProviderAccount(
            String providerAccountId,
            @NonNull Instant now
    ) {
        requireText(providerAccountId, "Provider account id must not be blank.");

        if (hasProviderAccount()) {
            throw new SettlementAccountAlreadyLinkedException();
        }

        this.providerAccountId = providerAccountId;
        creationStartedAt = null;
        status = SettlementAccountStatus.PENDING_ONBOARDING;
        requiredAction = SettlementAccountRequiredAction.CONTINUE_ONBOARDING;
        updatedAt = now;
    }

    public boolean canRequestOnboardingLink() {
        return hasProviderAccount()
                && !status.isDisabled()
                && requiredAction.requiresOnboarding();
    }

    public SettlementAccountUpdateResult recordProviderUpdate(
            boolean payoutsEnabled,
            boolean transfersCapabilityActive,
            String disabledReason,
            @NonNull SettlementAccountRequirements requirements,
            @NonNull Instant eventAt,
            @NonNull Instant now
    ) {
        if (!hasProviderAccount()) {
            throw new SettlementAccountNotLinkedException();
        }

        if (lastProviderEventAt != null
                && eventAt.isBefore(lastProviderEventAt)) {
            return new SettlementAccountUpdateResult.Stale();
        }

        SettlementAccountState previousState = state();

        status = resolveStatus(payoutsEnabled, disabledReason);
        requiredAction = SettlementAccountRequiredAction.resolve(
                status,
                requirements
        );
        statusReason = disabledReason;
        this.transfersCapabilityActive = transfersCapabilityActive;
        lastProviderEventAt = eventAt;
        updatedAt = now;

        SettlementAccountState currentState = state();

        if (!currentState.hasChangedSince(previousState)) {
            return new SettlementAccountUpdateResult.AppliedWithoutStateChange();
        }

        return new SettlementAccountUpdateResult.StateChanged(
                new SettlementAccountStateChanged(
                        id,
                        userId,
                        previousState,
                        currentState,
                        now
                )
        );
    }

    private SettlementAccountStatus resolveStatus(
            boolean payoutsEnabled,
            String disabledReason
    ) {
        if (!hasRestriction(disabledReason) && payoutsEnabled) {
            return SettlementAccountStatus.ACTIVE;
        }

        return status == SettlementAccountStatus.PENDING_ONBOARDING
                ? SettlementAccountStatus.PENDING_ONBOARDING
                : SettlementAccountStatus.RESTRICTED;
    }

    private boolean hasRestriction(String reason) {
        return reason != null && !reason.isBlank();
    }
}
