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
                .findByPaymentAccountId(
                        account.paymentAccountId()
                )
                .ifPresentOrElse(

                        merchant -> activateMerchant(
                                merchant,
                                account
                        ),

                        () -> log.warn(
                                "Ignoring payment account update for unknown payment account [{}].",
                                account.paymentAccountId()
                        )
                );
    }

    private void activateMerchant(
            Merchant merchant,
            PaymentAccountUpdated account
    ) {

        if (!account.isFullyEnabled()) {

            log.debug(
                    "Payment account [{}] is not fully enabled yet. chargesEnabled={}, payoutsEnabled={}",
                    account.paymentAccountId(),
                    account.chargesEnabled(),
                    account.payoutsEnabled()
            );

            return;
        }

        /*
         * Only a PENDING merchant may be activated.
         *
         * This makes duplicate webhook deliveries harmless and prevents
         * an account.updated event from causing an error for a SUSPENDED
         * merchant.
         */
        if (!merchant.isPending()) {

            log.debug(
                    "Ignoring payment account update for merchant [{}] in status [{}].",
                    merchant.getId(),
                    merchant.getStatus()
            );

            return;
        }

        merchant.activate(
                timeProvider.now()
        );

        merchantRepository.update(
                merchant
        );

        log.info(
                "Merchant [{}] activated after payment account [{}] became fully enabled.",
                merchant.getId(),
                account.paymentAccountId()
        );
    }
}