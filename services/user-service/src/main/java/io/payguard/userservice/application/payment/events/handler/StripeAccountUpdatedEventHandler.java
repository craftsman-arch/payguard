package io.payguard.userservice.application.payment.events.handler;

import io.payguard.userservice.application.payment.events.StripeEventHandler;
import io.payguard.userservice.application.payment.events.StripeEventType;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.MerchantRepository;
import io.payguard.userservice.integration.payment.events.dto.StripeAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class StripeAccountUpdatedEventHandler implements StripeEventHandler<StripeAccount> {

    private final MerchantRepository merchantRepository;
    private final TimeProvider timeProvider;

    @Override
    public StripeEventType supports() {
        return StripeEventType.ACCOUNT_UPDATED;
    }

    @Override
    public Class<StripeAccount> eventClass() {
        return StripeAccount.class;
    }

    @Override
    public void handle(StripeAccount account) {

        merchantRepository
                .findByPaymentAccountId(
                        account.id()
                )
                .ifPresentOrElse(

                        merchant -> activateMerchant(
                                merchant,
                                account
                        ),

                        () -> log.warn(
                                "Ignoring Stripe account.updated event for unknown payment account [{}].",
                                account.id()
                        )
                );
    }

    private void activateMerchant(Merchant merchant, StripeAccount account) {

        if (!account.isFullyEnabled()) {

            log.debug(
                    "Stripe account [{}] is not fully enabled yet. chargesEnabled={}, payoutsEnabled={}",
                    account.id(),
                    account.chargesEnabled(),
                    account.payoutsEnabled()
            );

            return;
        }

        if (merchant.isActive()) {

            log.debug("Merchant [{}] is already active.", merchant.getId());
            return;
        }

        merchant.activate(timeProvider.now());
        merchantRepository.update(merchant);

        log.info(
                "Merchant [{}] activated after Stripe account [{}] became fully enabled.",
                merchant.getId(),
                account.id()
        );
    }

}