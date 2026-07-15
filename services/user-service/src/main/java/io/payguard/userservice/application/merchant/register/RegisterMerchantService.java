package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.domain.merchant.Merchant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterMerchantService {

    private final MerchantRegistrationService registrationService;
    private final MerchantIdentityProvisioningService identityProvisioningService;

    public RegisterMerchantResult execute(RegisterMerchantCommand command) {

        Merchant merchant = registrationService.register(command);

        identityProvisioningService.provision(merchant);

        return new RegisterMerchantResult(
                merchant.getId()
        );
    }

}