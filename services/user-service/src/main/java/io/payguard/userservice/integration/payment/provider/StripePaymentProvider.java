package io.payguard.userservice.integration.payment.provider;

import io.payguard.userservice.application.payment.PaymentProvider;
import io.payguard.userservice.application.payment.SettlementAccountOnboardingRequest;
import io.payguard.userservice.application.payment.SettlementAccountProvisioningRequest;
import io.payguard.userservice.integration.payment.account.mapper.StripeAccountMapper;
import io.payguard.userservice.integration.payment.client.StripeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripePaymentProvider implements PaymentProvider {

    private final StripeClient stripeClient;
    private final StripeAccountMapper mapper;

    @Override
    public String createSettlementAccount(
            SettlementAccountProvisioningRequest request
    ) {

        return stripeClient.createAccount(
                mapper.toCreateAccountRequest(request),
                request.idempotencyKey()
        );
    }

    @Override
    public String createSettlementAccountOnboardingLink(
            SettlementAccountOnboardingRequest request
    ) {

        return stripeClient.createAccountLink(
                mapper.toCreateAccountLinkRequest(request)
        );
    }

}
