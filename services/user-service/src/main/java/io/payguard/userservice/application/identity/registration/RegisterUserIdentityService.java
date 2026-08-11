package io.payguard.userservice.application.identity.registration;

import io.payguard.userservice.application.identity.IdentityProvider;
import io.payguard.userservice.application.identity.IdentityRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserIdentityService {

    private final IdentityProvider identityProvider;

    public void execute(RegisterUserIdentityCommand command) {

        String identityUserId = identityProvider.createUser(command.email(), command.password());

        identityProvider.assignRealmRole(identityUserId, IdentityRole.USER);
        identityProvider.sendVerificationEmail(identityUserId);
    }
}
