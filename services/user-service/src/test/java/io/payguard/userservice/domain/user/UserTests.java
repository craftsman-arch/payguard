package io.payguard.userservice.domain.user;

import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.domain.user.exception.InvalidDisplayNameException;
import io.payguard.userservice.domain.user.exception.InvalidUserRevisionException;
import io.payguard.userservice.domain.user.exception.UserAlreadyActiveException;
import io.payguard.userservice.domain.user.exception.UserAlreadySuspendedException;
import io.payguard.userservice.domain.user.value.DisplayName;
import io.payguard.userservice.domain.user.value.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTests {

    private static final UserId USER_ID = UserId.of(UUID.fromString("7f895b10-d59f-43e5-96e7-0ae9658f90b0"));
    private static final Email EMAIL = Email.of("user@test.com");
    private static final DisplayName DISPLAY_NAME = DisplayName.of("Sergei");
    private static final Country MARKET_COUNTRY = Country.of("GB");
    private static final String IDENTITY_USER_ID = "keycloak-user-id";
    private static final Instant CREATED_AT = Instant.parse("2026-08-11T08:00:00Z");

    @Test
    void registersActiveUserForVerifiedIdentity() {

        User user = registeredUser();

        assertThat(user.getId()).isEqualTo(USER_ID);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(user.getMarketCountry()).isEqualTo(MARKET_COUNTRY);
        assertThat(user.getIdentityUserId()).isEqualTo(IDENTITY_USER_ID);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRevision()).isZero();
        assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(user.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void suspendsAndReactivatesUser() {

        User user = registeredUser();
        Instant suspendedAt = CREATED_AT.plusSeconds(60);
        Instant reactivatedAt = suspendedAt.plusSeconds(60);

        user.suspend(suspendedAt);

        assertThat(user.isSuspended()).isTrue();
        assertThat(user.getUpdatedAt()).isEqualTo(suspendedAt);

        user.reactivate(reactivatedAt);

        assertThat(user.isActive()).isTrue();
        assertThat(user.getUpdatedAt()).isEqualTo(reactivatedAt);
    }

    @Test
    void rejectsRepeatedStateTransitions() {

        User activeUser = registeredUser();

        assertThatThrownBy(() -> activeUser.reactivate(CREATED_AT))
                .isInstanceOf(UserAlreadyActiveException.class);

        activeUser.suspend(CREATED_AT.plusSeconds(60));

        assertThatThrownBy(() -> activeUser.suspend(CREATED_AT.plusSeconds(120)))
                .isInstanceOf(UserAlreadySuspendedException.class);
    }

    @Test
    void restoresPersistedUserRevisionAndState() {

        Instant updatedAt = CREATED_AT.plusSeconds(60);

        User user = User.restore(
                USER_ID,
                EMAIL,
                DISPLAY_NAME,
                MARKET_COUNTRY,
                IDENTITY_USER_ID,
                UserStatus.SUSPENDED,
                4,
                CREATED_AT,
                updatedAt
        );

        assertThat(user.isSuspended()).isTrue();
        assertThat(user.getRevision()).isEqualTo(4);
        assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void rejectsNegativePersistedRevision() {

        assertThatThrownBy(() -> User.restore(
                USER_ID,
                EMAIL,
                DISPLAY_NAME,
                MARKET_COUNTRY,
                IDENTITY_USER_ID,
                UserStatus.ACTIVE,
                -1,
                CREATED_AT,
                CREATED_AT
        )).isInstanceOf(InvalidUserRevisionException.class);
    }

    @Test
    void normalizesAndValidatesDisplayName() {

        assertThat(DisplayName.of("  Sergei  ").value())
                .isEqualTo("Sergei");

        assertThatThrownBy(() -> DisplayName.of(" "))
                .isInstanceOf(InvalidDisplayNameException.class);

        assertThatThrownBy(() -> DisplayName.of("a".repeat(101)))
                .isInstanceOf(InvalidDisplayNameException.class);
    }

    private static User registeredUser() {

        return User.registerForIdentity(
                USER_ID,
                EMAIL,
                DISPLAY_NAME,
                MARKET_COUNTRY,
                IDENTITY_USER_ID,
                CREATED_AT
        );
    }
}
