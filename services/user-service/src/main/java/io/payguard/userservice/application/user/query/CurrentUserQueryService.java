package io.payguard.userservice.application.user.query;

import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.domain.settlement.SettlementAccount;
import io.payguard.userservice.domain.settlement.SettlementAccountRepository;
import io.payguard.userservice.domain.settlement.exception.SettlementAccountNotFoundException;
import io.payguard.userservice.domain.user.User;
import io.payguard.userservice.domain.user.UserRepository;
import io.payguard.userservice.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CurrentUserQueryService {

    private final UserRepository userRepository;
    private final SettlementAccountRepository settlementAccountRepository;
    private final CurrentUserProvider currentUserProvider;

    public CurrentUserResult execute(CurrentUserQuery query) {
        String identityUserId = currentUserProvider.currentUserId();

        User user = userRepository
                .findByIdentityUserId(identityUserId)
                .orElseThrow(() -> new UserNotFoundException(
                        identityUserId
                ));

        SettlementAccount settlementAccount = settlementAccountRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new SettlementAccountNotFoundException(
                        user.getId()
                ));

        return new CurrentUserResult(
                user.getId().value(),
                user.getEmail().getValue(),
                user.getDisplayName().value(),
                user.getMarketCountry().getValue(),
                user.getStatus(),
                toResult(settlementAccount),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private CurrentSettlementAccountResult toResult(
            SettlementAccount account
    ) {
        return new CurrentSettlementAccountResult(
                account.getId().value(),
                account.getProvider(),
                account.getProviderAccountId(),
                account.getAccountHolderName().value(),
                account.getAccountHolderType(),
                account.getCountry().getValue(),
                account.getStatus(),
                account.getStatusReason(),
                account.getRequiredAction(),
                account.canReceiveTransfers(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
