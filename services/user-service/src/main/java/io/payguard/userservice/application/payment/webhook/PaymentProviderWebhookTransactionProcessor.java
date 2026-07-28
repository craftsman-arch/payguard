package io.payguard.userservice.application.payment.webhook;

import io.payguard.userservice.application.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProviderWebhookTransactionProcessor {

    private final ProcessedPaymentProviderEventRepository processedEventRepository;
    private final PaymentProviderWebhookDispatcher dispatcher;
    private final TimeProvider timeProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(PaymentProviderWebhook webhook) {

        boolean claimed = processedEventRepository.tryClaim(
                webhook.eventId(),
                webhook.type(),
                timeProvider.now()
        );

        if (!claimed) {

            log.info(
                    "Ignoring already processed payment-provider webhook [{}] with event id [{}].",
                    webhook.type(),
                    webhook.eventId()
            );

            return;
        }

        dispatcher.dispatch(webhook);
    }
}
