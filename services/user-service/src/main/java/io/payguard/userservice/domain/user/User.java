package io.payguard.userservice.domain.user;

import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.common.value.Email;
import io.payguard.userservice.domain.user.exception.InvalidUserRevisionException;
import io.payguard.userservice.domain.user.exception.UserAlreadyActiveException;
import io.payguard.userservice.domain.user.exception.UserAlreadySuspendedException;
import io.payguard.userservice.domain.user.value.DisplayName;
import io.payguard.userservice.domain.user.value.UserId;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static io.payguard.userservice.common.validation.TextPreconditions.requireText;

@Getter
public class User {

    private final UserId id;
    private final Email email;
    private final DisplayName displayName;
    private final Country marketCountry;
    private final String identityUserId;
    private final long revision;
    private final Instant createdAt;

    private UserStatus status;
    private Instant updatedAt;

    private User(
            UserId id,
            Email email,
            DisplayName displayName,
            Country marketCountry,
            String identityUserId,
            UserStatus status,
            long revision,
            Instant createdAt,
            Instant updatedAt
    ) {

        requireText(identityUserId, "Identity user id must not be blank.");

        if (revision < 0) {
            throw new InvalidUserRevisionException(revision);
        }

        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.marketCountry = marketCountry;
        this.identityUserId = identityUserId;
        this.status = status;
        this.revision = revision;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User registerForIdentity(
            @NonNull UserId id,
            @NonNull Email email,
            @NonNull DisplayName displayName,
            @NonNull Country marketCountry,
            @NonNull String identityUserId,
            @NonNull Instant now
    ) {

        return new User(
                id,
                email,
                displayName,
                marketCountry,
                identityUserId,
                UserStatus.ACTIVE,
                0,
                now,
                now
        );
    }

    public static User restore(
            @NonNull UserId id,
            @NonNull Email email,
            @NonNull DisplayName displayName,
            @NonNull Country marketCountry,
            @NonNull String identityUserId,
            @NonNull UserStatus status,
            long revision,
            @NonNull Instant createdAt,
            @NonNull Instant updatedAt
    ) {

        return new User(
                id,
                email,
                displayName,
                marketCountry,
                identityUserId,
                status,
                revision,
                createdAt,
                updatedAt
        );
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }

    public void suspend(@NonNull Instant now) {

        if (isSuspended()) {
            throw new UserAlreadySuspendedException();
        }

        status = UserStatus.SUSPENDED;
        updatedAt = now;
    }

    public void reactivate(@NonNull Instant now) {

        if (isActive()) {
            throw new UserAlreadyActiveException();
        }

        status = UserStatus.ACTIVE;
        updatedAt = now;
    }
}
