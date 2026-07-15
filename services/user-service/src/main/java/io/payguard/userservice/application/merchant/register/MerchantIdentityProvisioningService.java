package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.application.identity.IdentityProvider;
import io.payguard.userservice.application.password.TemporaryPasswordGenerator;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantIdentityProvisioningService {

    private final MerchantRepository merchantRepository;
    private final IdentityProvider identityProvider;
    private final TemporaryPasswordGenerator passwordGenerator;
    private final TimeProvider timeProvider;

    @Transactional
    public void provision(Merchant merchant) {

        if (!merchant.canProvisionIdentity()) {
            return;
        }

        String identityUserId = null;

        try {

            identityUserId = identityProvider.createUser(
                    merchant.getEmail(),
                    passwordGenerator.generate()
            );

            merchant.activate(
                    identityUserId,
                    timeProvider.now()
            );

            merchantRepository.update(merchant);

        } catch (RuntimeException ex) {

            compensateIdentityProvision(identityUserId);

            throw ex;
        }
    }

    private void compensateIdentityProvision(String identityUserId) {

        if (identityUserId == null) {
            return;
        }

        try {

            identityProvider.deleteUser(identityUserId);

        } catch (RuntimeException ex) {

            log.error(
                    "Failed to compensate Keycloak user [{}]. Manual reconciliation may be required.",
                    identityUserId,
                    ex
            );
        }
    }

}