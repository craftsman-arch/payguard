package io.payguard.userservice.application.identity.registration;

import io.payguard.userservice.application.identity.IdentityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResendMerchantVerificationEmailService {

    private final IdentityProvider identityProvider;

    public void execute(ResendMerchantVerificationEmailCommand command) {

        identityProvider.resumeMerchantIdentityRegistration(
                command.email()
        );
    }
}
