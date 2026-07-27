package io.payguard.userservice.integration.payment.account.mapper;

import io.payguard.userservice.domain.merchant.Merchant;
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

    public StripeAccountRequest toCreateAccountRequest(Merchant merchant) {

        return new StripeAccountRequest(
                "express",
                merchant.getCountry().getValue(),
                merchant.getEmail().getValue(),
                merchant.getBusinessType()
                        .name()
                        .toLowerCase(Locale.ROOT)
        );
    }

    public StripeAccountLinkRequest toCreateAccountLinkRequest(Merchant merchant) {

        return new StripeAccountLinkRequest(
                merchant.getPaymentAccountId(),
                properties.refreshUrl(),
                properties.returnUrl(),
                "account_onboarding"
        );
    }

}
