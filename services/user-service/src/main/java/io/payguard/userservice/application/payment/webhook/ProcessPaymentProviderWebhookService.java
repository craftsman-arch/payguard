package io.payguard.userservice.application.payment.webhook;

import io.payguard.userservice.domain.merchant.exception.ConcurrentMerchantModificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import static io.payguard.userservice.application.payment.webhook.PaymentProviderWebhookConcurrencyRetryConfiguration.RETRY_TEMPLATE;

@Service
@Slf4j
public class ProcessPaymentProviderWebhookService {

    private final PaymentProviderWebhookVerifier verifier;
    private final PaymentProviderWebhookParser parser;
    private final PaymentProviderWebhookTransactionProcessor transactionProcessor;
    private final RetryTemplate concurrencyRetryTemplate;

    public ProcessPaymentProviderWebhookService(
            PaymentProviderWebhookVerifier verifier,
            PaymentProviderWebhookParser parser,
            PaymentProviderWebhookTransactionProcessor transactionProcessor,
            @Qualifier(RETRY_TEMPLATE)
            RetryTemplate concurrencyRetryTemplate
    ) {
        this.verifier = verifier;
        this.parser = parser;
        this.transactionProcessor = transactionProcessor;
        this.concurrencyRetryTemplate = concurrencyRetryTemplate;
    }

    public void execute(ProcessPaymentProviderWebhookCommand command) {

        verifier.verify(command.payload(), command.signature());
        PaymentProviderWebhook webhook = parser.parse(command.payload());

        processWithConcurrencyRetry(webhook);
    }

    private void processWithConcurrencyRetry(PaymentProviderWebhook webhook) {

        try {

            concurrencyRetryTemplate.execute(context -> {

                if (context.getRetryCount() > 0) {

                    log.debug(
                            "Retrying payment-provider webhook [{}] with event id [{}] after merchant concurrency conflict; attempt [{}].",
                            webhook.type(),
                            webhook.eventId(),
                            context.getRetryCount() + 1
                    );
                }

                transactionProcessor.process(webhook);
                return null;
            });

        } catch (
                ConcurrentMerchantModificationException exception
        ) {

            log.warn(
                    "Payment-provider webhook [{}] with event id [{}] exhausted merchant concurrency retries.",
                    webhook.type(),
                    webhook.eventId()
            );

            throw exception;
        }
    }
}
