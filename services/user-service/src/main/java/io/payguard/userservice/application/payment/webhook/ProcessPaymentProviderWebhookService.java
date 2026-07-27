package io.payguard.userservice.application.payment.webhook;

import io.payguard.userservice.application.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProcessPaymentProviderWebhookService {

    private final PaymentProviderWebhookVerifier verifier;
    private final PaymentProviderWebhookParser parser;
    private final ProcessedPaymentProviderEventRepository processedEventRepository;
    private final PaymentProviderWebhookDispatcher dispatcher;
    private final TimeProvider timeProvider;

    public void execute(ProcessPaymentProviderWebhookCommand command) {

        verifier.verify(command.payload(), command.signature());
        PaymentProviderWebhook webhook = parser.parse(command.payload());

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