package io.payguard.userservice.domain.settlement;

import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.domain.settlement.exception.SettlementAccountAlreadyLinkedException;
import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.value.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementAccountTests {

    private static final SettlementAccountId ACCOUNT_ID = SettlementAccountId.of(UUID.fromString("675f0539-afdf-4f2a-a67d-6328ce076976"));
    private static final UserId USER_ID = UserId.of(UUID.fromString("ec9ed38c-e6ab-414b-bf62-d42f93471574"));
    private static final Instant CREATED_AT = Instant.parse("2026-08-12T08:00:00Z");

    @Test
    void opensPendingSettlementAccountForUser() {

        SettlementAccount account = openAccount();

        assertThat(account.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(account.getUserId()).isEqualTo(USER_ID);
        assertThat(account.getProvider()).isEqualTo(SettlementProvider.STRIPE);
        assertThat(account.getStatus()).isEqualTo(SettlementAccountStatus.PENDING_ONBOARDING);
        assertThat(account.getRequiredAction()).isEqualTo(SettlementAccountRequiredAction.CONTINUE_ONBOARDING);
        assertThat(account.hasProviderAccount()).isFalse();
        assertThat(account.canReceiveTransfers()).isFalse();
        assertThat(account.getRevision()).isZero();
    }

    @Test
    void tracksProviderAccountProvisioning() {

        SettlementAccount account = openAccount();
        Instant startedAt = CREATED_AT.plusSeconds(30);
        Instant linkedAt = startedAt.plusSeconds(30);

        account.recordCreationStarted(startedAt);
        account.recordCreationStarted(startedAt.plusSeconds(10));

        assertThat(account.getCreationStartedAt()).isEqualTo(startedAt);

        account.linkProviderAccount("acct_123", linkedAt);

        assertThat(account.getProviderAccountId()).isEqualTo("acct_123");
        assertThat(account.getCreationStartedAt()).isNull();
        assertThat(account.canRequestOnboardingLink()).isTrue();

        assertThatThrownBy(
                () -> account.linkProviderAccount("acct_456", linkedAt.plusSeconds(10)))
                .isInstanceOf(SettlementAccountAlreadyLinkedException.class);
    }

    @Test
    void activatesTransferEligibilityFromProviderState() {

        SettlementAccount account = linkedAccount();
        Instant eventAt = CREATED_AT.plusSeconds(120);
        Instant processedAt = eventAt.plusSeconds(1);

        SettlementAccountUpdateResult result = account.recordProviderUpdate(
                true,
                true,
                null,
                SettlementAccountRequirements.empty(),
                eventAt,
                processedAt
        );

        assertThat(result).isInstanceOf(SettlementAccountUpdateResult.StateChanged.class);
        assertThat(account.getStatus()).isEqualTo(SettlementAccountStatus.ACTIVE);
        assertThat(account.getRequiredAction()).isEqualTo(SettlementAccountRequiredAction.NONE);
        assertThat(account.canReceiveTransfers()).isTrue();
        assertThat(result.domainEvents()).hasSize(1);
    }

    @Test
    void recordsPendingVerificationWithoutUserAction() {

        SettlementAccount account = linkedAccount();

        account.recordProviderUpdate(
                false,
                false,
                null,
                new SettlementAccountRequirements(
                        false,
                        true,
                        false
                ),
                CREATED_AT.plusSeconds(120),
                CREATED_AT.plusSeconds(121)
        );

        assertThat(account.getRequiredAction()).isEqualTo(SettlementAccountRequiredAction.WAIT_FOR_REVIEW);
        assertThat(account.canReceiveTransfers()).isFalse();
    }

    @Test
    void ignoresProviderEventOlderThanLastAppliedEvent() {

        SettlementAccount account = linkedAccount();
        Instant latestEventAt = CREATED_AT.plusSeconds(180);

        account.recordProviderUpdate(
                true,
                true,
                null,
                SettlementAccountRequirements.empty(),
                latestEventAt,
                latestEventAt.plusSeconds(1)
        );

        SettlementAccountUpdateResult result = account.recordProviderUpdate(
                false,
                false,
                "requirements.past_due",
                SettlementAccountRequirements.empty(),
                latestEventAt.minusSeconds(1),
                latestEventAt.plusSeconds(2)
        );

        assertThat(result).isInstanceOf(SettlementAccountUpdateResult.Stale.class);
        assertThat(account.canReceiveTransfers()).isTrue();
        assertThat(account.getStatus()).isEqualTo(SettlementAccountStatus.ACTIVE);
    }

    @Test
    void appliesProviderEventWithoutEmittingUnchangedBusinessState() {

        SettlementAccount account = linkedAccount();

        SettlementAccountUpdateResult result = account.recordProviderUpdate(
                false,
                false,
                null,
                SettlementAccountRequirements.empty(),
                CREATED_AT.plusSeconds(120),
                CREATED_AT.plusSeconds(121)
        );

        assertThat(result).isInstanceOf(SettlementAccountUpdateResult.AppliedWithoutStateChange.class);
        assertThat(result.wasApplied()).isTrue();
        assertThat(result.domainEvents()).isEmpty();
    }

    private static SettlementAccount linkedAccount() {

        SettlementAccount account = openAccount();
        account.linkProviderAccount("acct_123", CREATED_AT.plusSeconds(60));
        return account;
    }

    private static SettlementAccount openAccount() {

        return SettlementAccount.open(
                ACCOUNT_ID,
                USER_ID,
                SettlementProvider.STRIPE,
                AccountHolderName.of("Sergei Suh"),
                SettlementAccountHolderType.INDIVIDUAL,
                Country.of("GB"),
                CREATED_AT
        );
    }
}
