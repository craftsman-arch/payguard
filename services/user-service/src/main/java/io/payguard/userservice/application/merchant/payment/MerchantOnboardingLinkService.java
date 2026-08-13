package io.payguard.userservice.application.merchant.payment;

import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.application.payment.PaymentProvider;
import io.payguard.userservice.application.payment.SettlementAccountOnboardingRequest;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.MerchantNotEligibleForReonboardingException;
import io.payguard.userservice.domain.merchant.exception.MerchantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantOnboardingLinkService {

    private final MerchantRepository merchantRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PaymentProvider paymentProvider;

    public MerchantOnboardingLink execute() {

        String identityUserId = currentUserProvider.currentUserId();

        Merchant merchant = merchantRepository
                .findByIdentityUserId(identityUserId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant with identity user id '%s' was not found."
                                .formatted(identityUserId)
                ));

        if (!merchant.canRequestPaymentAccountOnboardingLink()) {
            throw new MerchantNotEligibleForReonboardingException();
        }

        String onboardingUrl =
                paymentProvider.createSettlementAccountOnboardingLink(
                        new SettlementAccountOnboardingRequest(
                                merchant.getPaymentAccountId()
                        )
                );

        return new MerchantOnboardingLink(onboardingUrl);
    }
}
