package io.payguard.userservice.application.merchant.profile;

import io.payguard.userservice.application.id.IdGenerator;
import io.payguard.userservice.application.identity.AuthenticatedUser;
import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.UnverifiedMerchantIdentityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateMerchantProfileService {

    private final MerchantRepository merchantRepository;
    private final CurrentUserProvider currentUserProvider;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public CreateMerchantProfileResult execute(CreateMerchantProfileCommand command) {

        AuthenticatedUser currentUser = currentUserProvider.currentUser();

        if (!currentUser.emailVerified()) {
            throw new UnverifiedMerchantIdentityException();
        }

        Merchant existingMerchant = merchantRepository
                .findByIdentityUserId(currentUser.id())
                .orElse(null);

        if (existingMerchant != null) {
            return toResult(existingMerchant, false);
        }

        return toResult(
                createMerchant(currentUser, command),
                true
        );
    }

    private CreateMerchantProfileResult toResult(Merchant merchant, boolean created) {

        return new CreateMerchantProfileResult(
                merchant.getId(),
                merchant.getStatus(),
                created
        );
    }

    private Merchant createMerchant(AuthenticatedUser currentUser, CreateMerchantProfileCommand command) {

        Merchant merchant = Merchant.registerForIdentity(
                idGenerator.generate(),
                currentUser.email(),
                command.legalName(),
                command.businessType(),
                command.country(),
                currentUser.id(),
                timeProvider.now()
        );

        merchantRepository.add(merchant);

        return merchant;
    }
}
