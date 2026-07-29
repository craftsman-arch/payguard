package io.payguard.userservice.application.merchant.payment;

import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.domain.merchant.exception.MerchantAlreadyLinkedToPaymentProviderException;
import io.payguard.userservice.domain.merchant.exception.MerchantNotFoundException;
import io.payguard.userservice.domain.merchant.exception.PaymentAccountReconciliationRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentAccountCreationTransactionService {

    private final MerchantRepository merchantRepository;
    private final TimeProvider timeProvider;
    private final PaymentAccountCreationProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PreparedPaymentAccountCreation prepare(String identityUserId) {

        Merchant merchant = findMerchant(identityUserId);

        if (merchant.hasPaymentAccount()) {
            return new PreparedPaymentAccountCreation(
                    merchant,
                    false
            );
        }

        Instant now = timeProvider.now();
        Instant creationStartedAt = merchant.getPaymentAccountCreationStartedAt();

        if (creationStartedAt != null
                && !now.isBefore(
                        creationStartedAt.plus(
                                properties.idempotencySafetyWindow()
                        )
                )) {

            throw new PaymentAccountReconciliationRequiredException(
                    merchant.getId()
            );
        }

        if (creationStartedAt == null) {

            merchant.recordPaymentAccountCreationStarted(now);
            merchantRepository.update(merchant);
        }

        return new PreparedPaymentAccountCreation(
                merchant,
                true
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Merchant confirm(String identityUserId, String paymentAccountId) {

        Merchant merchant = findMerchant(identityUserId);

        if (merchant.hasPaymentAccount()) {

            if (!merchant.getPaymentAccountId().equals(paymentAccountId)) {

                throw new MerchantAlreadyLinkedToPaymentProviderException();
            }

            return merchant;
        }

        merchant.linkPaymentProvider(paymentAccountId, timeProvider.now());
        merchantRepository.update(merchant);

        return merchant;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearDefinitiveFailure(String identityUserId) {

        Merchant merchant = findMerchant(identityUserId);

        if (merchant.hasPaymentAccount()
                || merchant.getPaymentAccountCreationStartedAt() == null) {
            return;
        }

        merchant.clearPaymentAccountCreationAttempt(timeProvider.now());
        merchantRepository.update(merchant);
    }

    private Merchant findMerchant(String identityUserId) {

        return merchantRepository
                .findByIdentityUserId(identityUserId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant with identity user id '%s' was not found."
                                .formatted(identityUserId)
                ));
    }
}
