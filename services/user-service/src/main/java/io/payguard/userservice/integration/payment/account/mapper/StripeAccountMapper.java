package io.payguard.userservice.integration.payment.account.mapper;

import io.payguard.userservice.application.payment.SettlementAccountOnboardingRequest;
import io.payguard.userservice.application.payment.SettlementAccountProvisioningRequest;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountLinkRequest;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountRequest;
import io.payguard.userservice.integration.payment.config.StripeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class StripeAccountMapper {

    private final StripeProperties properties;

    public StripeAccountRequest toCreateAccountRequest(
            SettlementAccountProvisioningRequest request
    ) {

        return new StripeAccountRequest(
                "express",
                request.country(),
                request.email(),
                request.accountHolderType()
                        .toLowerCase(Locale.ROOT)
        );
    }

    public StripeAccountLinkRequest toCreateAccountLinkRequest(
            SettlementAccountOnboardingRequest request
    ) {

        return new StripeAccountLinkRequest(
                request.providerAccountId(),
                properties.refreshUrl(),
                properties.returnUrl(),
                "account_onboarding"
        );
    }

}
