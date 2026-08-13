package io.payguard.userservice.application.user.query;

import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.domain.settlement.SettlementAccount;
import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.payguard.userservice.domain.settlement.SettlementAccountRepository;
import io.payguard.userservice.domain.settlement.SettlementProvider;
import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.User;
import io.payguard.userservice.domain.user.UserRepository;
import io.payguard.userservice.domain.user.exception.UserNotFoundException;
import io.payguard.userservice.domain.user.value.DisplayName;
import io.payguard.userservice.domain.user.value.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CurrentUserQueryServiceTests {

    private static final String IDENTITY_USER_ID = "keycloak-user-id";
    private static final UserId USER_ID = UserId.of(UUID.fromString("7f895b10-d59f-43e5-96e7-0ae9658f90b0"));
    private static final Instant NOW = Instant.parse("2026-08-13T08:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private SettlementAccountRepository settlementAccountRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CurrentUserQueryService service;

    @Test
    void returnsUserWithSeparateSettlementAccountState() {
        User user = user();
        SettlementAccount account = settlementAccount();

        given(currentUserProvider.currentUserId()).willReturn(IDENTITY_USER_ID);
        given(userRepository.findByIdentityUserId(IDENTITY_USER_ID)).willReturn(Optional.of(user));
        given(settlementAccountRepository.findByUserId(USER_ID)).willReturn(Optional.of(account));

        CurrentUserResult result = service.execute(new CurrentUserQuery());

        assertThat(result.id()).isEqualTo(USER_ID.value());
        assertThat(result.displayName()).isEqualTo("Alex");
        assertThat(result.settlementAccount().provider()).isEqualTo(SettlementProvider.STRIPE);
        assertThat(result.settlementAccount().providerAccountId()).isNull();
        assertThat(result.settlementAccount().canReceiveTransfers()).isFalse();
    }

    @Test
    void stopsWhenUserDoesNotExist() {
        given(currentUserProvider.currentUserId()).willReturn(IDENTITY_USER_ID);
        given(userRepository.findByIdentityUserId(IDENTITY_USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new CurrentUserQuery())).isInstanceOf(UserNotFoundException.class);

        verify(settlementAccountRepository, never()).findByUserId(org.mockito.ArgumentMatchers.any());
    }

    private static User user() {

        return User.registerForIdentity(
                USER_ID,
                Email.of("user@test.com"),
                DisplayName.of("Alex"),
                Country.of("GB"),
                IDENTITY_USER_ID,
                NOW
        );
    }

    private static SettlementAccount settlementAccount() {

        return SettlementAccount.open(
                SettlementAccountId.of(UUID.fromString(
                        "3cf089b0-6222-46fa-b779-93d3ecdfb899"
                )),
                USER_ID,
                SettlementProvider.STRIPE,
                AccountHolderName.of("Alex Smith"),
                SettlementAccountHolderType.INDIVIDUAL,
                Country.of("GB"),
                NOW
        );
    }
}
