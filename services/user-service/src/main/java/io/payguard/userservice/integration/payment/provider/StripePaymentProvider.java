package io.payguard.userservice.integration.payment.provider;

import io.payguard.userservice.application.payment.PaymentProvider;
import io.payguard.userservice.domain.merchant.Merchant;
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
    public String createMerchantAccount(Merchant merchant) {

        return stripeClient.createAccount(

                mapper.toCreateAccountRequest(
                        merchant
                ),

                "merchant-account:"
                        + merchant.getId()
        );
    }

    @Override
    public String createMerchantOnboardingLink(Merchant merchant) {

        return stripeClient.createAccountLink(mapper.toCreateAccountLinkRequest(merchant));
    }

}
