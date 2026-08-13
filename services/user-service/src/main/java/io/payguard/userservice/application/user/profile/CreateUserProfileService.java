package io.payguard.userservice.application.user.profile;

import io.payguard.userservice.application.id.IdGenerator;
import io.payguard.userservice.application.identity.AuthenticatedUser;
import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.settlement.SettlementAccount;
import io.payguard.userservice.domain.settlement.SettlementAccountRepository;
import io.payguard.userservice.domain.settlement.SettlementProvider;
import io.payguard.userservice.domain.settlement.value.SettlementAccountId;
import io.payguard.userservice.domain.user.User;
import io.payguard.userservice.domain.user.UserRepository;
import io.payguard.userservice.domain.user.exception.UnverifiedUserIdentityException;
import io.payguard.userservice.domain.user.value.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateUserProfileService {

    private final UserRepository userRepository;
    private final SettlementAccountRepository settlementAccountRepository;
    private final CurrentUserProvider currentUserProvider;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public CreateUserProfileResult execute(CreateUserProfileCommand command) {

        AuthenticatedUser currentUser = currentUserProvider.currentUser();

        if (!currentUser.emailVerified()) {
            throw new UnverifiedUserIdentityException();
        }

        User existingUser = userRepository
                .findByIdentityUserId(currentUser.id())
                .orElse(null);

        if (existingUser != null) {
            return toResult(existingUser, false);
        }

        Instant now = timeProvider.now();
        UserId userId = new UserId(idGenerator.generate());

        User user = User.registerForIdentity(
                userId,
                currentUser.email(),
                command.displayName(),
                command.country(),
                currentUser.id(),
                now
        );

        SettlementAccount settlementAccount = SettlementAccount.open(
                new SettlementAccountId(idGenerator.generate()),
                userId,
                SettlementProvider.STRIPE,
                command.accountHolderName(),
                command.accountHolderType(),
                command.country(),
                now
        );

        userRepository.add(user);
        settlementAccountRepository.add(settlementAccount);

        return toResult(user, true);
    }

    private CreateUserProfileResult toResult(User user, boolean created) {

        return new CreateUserProfileResult(
                user.getId().value(),
                user.getStatus(),
                created
        );
    }
}
