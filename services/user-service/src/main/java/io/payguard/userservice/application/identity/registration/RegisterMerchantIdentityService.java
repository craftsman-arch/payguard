package io.payguard.userservice.application.identity.registration;

import io.payguard.userservice.application.identity.IdentityProvider;
import io.payguard.userservice.application.identity.IdentityRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterMerchantIdentityService {

    private final IdentityProvider identityProvider;

    public void execute(RegisterMerchantIdentityCommand command) {

        String identityUserId = identityProvider.createUser(command.email(), command.password());

        identityProvider.assignRealmRole(identityUserId, IdentityRole.MERCHANT);
        identityProvider.sendVerificationEmail(identityUserId);
    }
}
