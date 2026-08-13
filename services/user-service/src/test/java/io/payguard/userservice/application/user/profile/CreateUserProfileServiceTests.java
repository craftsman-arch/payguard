package io.payguard.userservice.application.user.profile;

import io.payguard.userservice.application.id.IdGenerator;
import io.payguard.userservice.application.identity.AuthenticatedUser;
import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.domain.settlement.SettlementAccount;
import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.payguard.userservice.domain.settlement.SettlementAccountRepository;
import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import io.payguard.userservice.domain.user.User;
import io.payguard.userservice.domain.user.UserRepository;
import io.payguard.userservice.domain.user.exception.UnverifiedUserIdentityException;
import io.payguard.userservice.domain.user.value.DisplayName;
import io.payguard.userservice.domain.user.value.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CreateUserProfileServiceTests {

    private static final String IDENTITY_USER_ID = "keycloak-user-id";
    private static final Email EMAIL = Email.of("user@test.com");
    private static final UUID USER_ID = UUID.fromString("7f895b10-d59f-43e5-96e7-0ae9658f90b0");
    private static final UUID SETTLEMENT_ACCOUNT_ID = UUID.fromString("3cf089b0-6222-46fa-b779-93d3ecdfb899");
    private static final Instant NOW = Instant.parse("2026-08-13T08:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private SettlementAccountRepository settlementAccountRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private CreateUserProfileService service;

    @Test
    void createsUserAndSettlementAccountForVerifiedIdentity() {
        given(currentUserProvider.currentUser()).willReturn(authenticatedUser(true));
        given(userRepository.findByIdentityUserId(IDENTITY_USER_ID)).willReturn(Optional.empty());
        given(timeProvider.now()).willReturn(NOW);
        given(idGenerator.generate()).willReturn(USER_ID, SETTLEMENT_ACCOUNT_ID);

        CreateUserProfileResult result = service.execute(command());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<SettlementAccount> accountCaptor = ArgumentCaptor.forClass(SettlementAccount.class);

        verify(userRepository).add(userCaptor.capture());
        verify(settlementAccountRepository).add(accountCaptor.capture());

        User user = userCaptor.getValue();
        SettlementAccount account = accountCaptor.getValue();

        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.created()).isTrue();
        assertThat(user.getIdentityUserId()).isEqualTo(IDENTITY_USER_ID);
        assertThat(account.getId().value()).isEqualTo(SETTLEMENT_ACCOUNT_ID);
        assertThat(account.getUserId()).isEqualTo(user.getId());
        assertThat(account.hasProviderAccount()).isFalse();
        assertThat(account.canReceiveTransfers()).isFalse();
    }

    @Test
    void returnsExistingUserWithoutCreatingResources() {

        User existingUser = existingUser();
        given(currentUserProvider.currentUser()).willReturn(authenticatedUser(true));
        given(userRepository.findByIdentityUserId(IDENTITY_USER_ID)).willReturn(Optional.of(existingUser));

        CreateUserProfileResult result = service.execute(command());

        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.created()).isFalse();
        verify(userRepository, never()).add(org.mockito.ArgumentMatchers.any());
        verify(settlementAccountRepository, never()).add(org.mockito.ArgumentMatchers.any());
        verify(idGenerator, never()).generate();
    }

    @Test
    void rejectsUnverifiedIdentityBeforePersistence() {
        given(currentUserProvider.currentUser()).willReturn(authenticatedUser(false));

        assertThatThrownBy(() -> service.execute(command())).isInstanceOf(UnverifiedUserIdentityException.class);

        verify(userRepository, never()).findByIdentityUserId(org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).add(org.mockito.ArgumentMatchers.any());
        verify(settlementAccountRepository, never()).add(org.mockito.ArgumentMatchers.any());

    }

    private static CreateUserProfileCommand command() {

        return new CreateUserProfileCommand(
                DisplayName.of("Alex"),
                AccountHolderName.of("Alex Smith"),
                SettlementAccountHolderType.INDIVIDUAL,
                Country.of("GB")
        );
    }

    private static AuthenticatedUser authenticatedUser(boolean verified) {

        return new AuthenticatedUser(IDENTITY_USER_ID, EMAIL, verified);
    }

    private static User existingUser() {

        return User.registerForIdentity(
                UserId.of(USER_ID),
                EMAIL,
                DisplayName.of("Alex"),
                Country.of("GB"),
                IDENTITY_USER_ID,
                NOW
        );
    }
}
