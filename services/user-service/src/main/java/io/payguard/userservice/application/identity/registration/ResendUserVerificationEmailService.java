package io.payguard.userservice.application.identity.registration;

import io.payguard.userservice.application.identity.IdentityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResendUserVerificationEmailService {

    private final IdentityProvider identityProvider;

    public void execute(ResendUserVerificationEmailCommand command) {

        identityProvider.resumeUserIdentityRegistration(
                command.email()
        );
    }
}
