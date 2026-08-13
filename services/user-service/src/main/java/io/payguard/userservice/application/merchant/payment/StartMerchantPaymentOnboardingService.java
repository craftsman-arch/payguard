package io.payguard.userservice.application.merchant.payment;

import io.payguard.userservice.application.identity.CurrentUserProvider;
import io.payguard.userservice.application.payment.PaymentProvider;
import io.payguard.userservice.application.payment.SettlementAccountOnboardingRequest;
import io.payguard.userservice.application.payment.SettlementAccountProvisioningRequest;
import io.payguard.userservice.domain.merchant.Merchant;
import io.payguard.userservice.domain.merchant.exception.ConcurrentMerchantModificationException;
import io.payguard.userservice.integration.payment.error.exception.StripeProviderRejectedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

import static io.payguard.userservice.application.merchant.concurrency.MerchantConcurrencyRetryConfiguration.RETRY_TEMPLATE;

@Slf4j
@Service
public class StartMerchantPaymentOnboardingService {

    private final CurrentUserProvider currentUserProvider;
    private final PaymentAccountCreationTransactionService transactionService;
    private final PaymentProvider paymentProvider;
    private final RetryTemplate concurrencyRetryTemplate;

    public StartMerchantPaymentOnboardingService(
            CurrentUserProvider currentUserProvider,
            PaymentAccountCreationTransactionService transactionService,
            PaymentProvider paymentProvider,
            @Qualifier(RETRY_TEMPLATE)
            RetryTemplate concurrencyRetryTemplate
    ) {
        this.currentUserProvider = currentUserProvider;
        this.transactionService = transactionService;
        this.paymentProvider = paymentProvider;
        this.concurrencyRetryTemplate = concurrencyRetryTemplate;
    }

    public MerchantOnboardingLink execute() {

        String identityUserId = currentUserProvider.currentUserId();
        PreparedPaymentAccountCreation preparation = prepareWithRetry(identityUserId);
        Merchant merchant = preparation.merchant();

        if (preparation.accountCreationRequired()) {
            merchant = createAndConfirmPaymentAccount(
                    identityUserId,
                    merchant
            );
        }

        String onboardingUrl = paymentProvider
                .createSettlementAccountOnboardingLink(
                        new SettlementAccountOnboardingRequest(
                                merchant.getPaymentAccountId()
                        )
                );

        return new MerchantOnboardingLink(onboardingUrl);
    }

    private Merchant createAndConfirmPaymentAccount(String identityUserId, Merchant merchant) {

        String paymentAccountId;

        try {

            paymentAccountId = paymentProvider.createSettlementAccount(
                    new SettlementAccountProvisioningRequest(
                            "merchant-account:" + merchant.getId(),
                            merchant.getEmail().getValue(),
                            merchant.getCountry().getValue(),
                            merchant.getBusinessType().name()
                    )
            );

        } catch (StripeProviderRejectedException exception) {

            executeWithConcurrencyRetry(
                    identityUserId,
                    () -> {
                        transactionService.clearDefinitiveFailure(
                                identityUserId
                        );
                        return null;
                    }
            );

            throw exception;
        }

        return executeWithConcurrencyRetry(
                identityUserId,
                () -> transactionService.confirm(
                        identityUserId,
                        paymentAccountId
                )
        );
    }

    private PreparedPaymentAccountCreation prepareWithRetry(String identityUserId) {
        return executeWithConcurrencyRetry(
                identityUserId,
                () -> transactionService.prepare(identityUserId)
        );
    }

    private <T> T executeWithConcurrencyRetry(String identityUserId, Supplier<T> action) {

        try {

            return concurrencyRetryTemplate.execute(context -> {

                if (context.getRetryCount() > 0) {
                    log.debug(
                            "Retrying payment-account provisioning for identity [{}] after merchant concurrency conflict; attempt [{}].",
                            identityUserId,
                            context.getRetryCount() + 1
                    );
                }

                return action.get();
            });

        } catch (ConcurrentMerchantModificationException exception) {

            log.warn(
                    "Payment-account provisioning for identity [{}] exhausted merchant concurrency retries.",
                    identityUserId
            );

            throw exception;
        }
    }
}
