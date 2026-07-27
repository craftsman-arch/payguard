package io.payguard.userservice.application.payment.webhook.handler;

import io.payguard.userservice.application.payment.webhook.PaymentAccountUpdated;
import io.payguard.userservice.application.payment.webhook.PaymentProviderEventType;
import io.payguard.userservice.application.payment.webhook.PaymentProviderWebhookHandler;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class PaymentAccountUpdatedEventHandler implements PaymentProviderWebhookHandler<PaymentAccountUpdated> {

    private final MerchantRepository merchantRepository;
    private final TimeProvider timeProvider;

    @Override
    public PaymentProviderEventType supports() {
        return PaymentProviderEventType.ACCOUNT_UPDATED;
    }

    @Override
    public Class<PaymentAccountUpdated> payloadType() {
        return PaymentAccountUpdated.class;
    }

    @Override
    public void handle(PaymentAccountUpdated account) {

        merchantRepository
                .findByPaymentAccountId(account.paymentAccountId())
                .ifPresentOrElse(
                        merchant -> updateMerchantPaymentReadiness(
                                merchant,
                                account
                        ),
                        () -> log.warn(
                                "Ignoring payment account update for unknown payment account [{}].",
                                account.paymentAccountId()
                        )
                );
    }

    private void updateMerchantPaymentReadiness(Merchant merchant, PaymentAccountUpdated account) {

        Instant now = timeProvider.now();

        boolean applied = merchant.recordPaymentAccountUpdate(
                account.payoutsEnabled(),
                account.cardPaymentsCapabilityActive(),
                account.transfersCapabilityActive(),
                account.disabledReason(),
                account.requirements(),
                account.eventAt(),
                now
        );

        if (!applied) {
            log.debug(
                    "Ignoring stale payment account update for merchant [{}].",
                    merchant.getId()
            );

            return;
        }

        if (merchant.getPaymentAccountStatus().isActive() && merchant.isPending()) {

            merchant.activate(now);
        }

        merchantRepository.update(merchant);

        log.info(
                "Recorded payment account [{}] status [{}] for merchant [{}].",
                account.paymentAccountId(),
                merchant.getPaymentAccountStatus(),
                merchant.getId()
        );
    }
}
