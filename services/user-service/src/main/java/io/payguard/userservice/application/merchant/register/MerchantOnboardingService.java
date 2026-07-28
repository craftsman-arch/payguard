package io.payguard.userservice.application.merchant.register;

import io.payguard.userservice.application.identity.IdentityProvider;
import io.payguard.userservice.application.identity.IdentityRole;
import io.payguard.userservice.application.payment.PaymentProvider;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MerchantOnboardingService {

    private final MerchantRepository merchantRepository;
    private final IdentityProvider identityProvider;
    private final PaymentProvider paymentProvider;
    private final TimeProvider timeProvider;

    public MerchantOnboardingResult onboard(
            Merchant merchant,
            String password
    ) {

        String identityUserId = null;
        String paymentAccountId = null;

        try {

            identityUserId = provisionIdentity(merchant, password);
            paymentAccountId = provisionPaymentAccount(merchant);
            merchantRepository.update(merchant);
            String onboardingUrl = paymentProvider.createMerchantOnboardingLink(merchant);

            return new MerchantOnboardingResult(onboardingUrl);

        } catch (RuntimeException ex) {

            compensate(identityUserId, paymentAccountId);
            throw ex;
        }
    }

    private String provisionIdentity(
            Merchant merchant,
            String password
    ) {

        if (!merchant.canProvisionIdentity()) {

            return merchant.getIdentityUserId();
        }

        String identityUserId = identityProvider.createUser(
                merchant.getEmail(),
                password
        );
        identityProvider.assignRealmRole(identityUserId, IdentityRole.MERCHANT);
        merchant.linkIdentity(identityUserId, timeProvider.now());

        return identityUserId;
    }

    private String provisionPaymentAccount(Merchant merchant) {

        if (!merchant.canProvisionPaymentAccount()) {
            return merchant.getPaymentAccountId();
        }

        String paymentAccountId = paymentProvider.createMerchantAccount(merchant);
        merchant.linkPaymentProvider(paymentAccountId, timeProvider.now());

        return paymentAccountId;
    }

    private void compensate(String identityUserId, String paymentAccountId) {

        compensatePaymentAccount(paymentAccountId);
        compensateIdentity(identityUserId);
    }

    private void compensatePaymentAccount(String paymentAccountId) {

        if (paymentAccountId == null) {
            return;
        }

        try {

            paymentProvider.deleteMerchantAccount(paymentAccountId);

        } catch (RuntimeException ex) {

            log.error(
                    "Failed to compensate Stripe account [{}]. Manual reconciliation may be required.",
                    paymentAccountId,
                    ex
            );
        }
    }

    private void compensateIdentity(String identityUserId) {

        if (identityUserId == null) {
            return;
        }

        try {

            identityProvider.deleteUser(identityUserId);

        } catch (RuntimeException ex) {

            log.error(
                    "Failed to compensate Keycloak user [{}]. Manual reconciliation may be required.",
                    identityUserId,
                    ex
            );
        }
    }

}
