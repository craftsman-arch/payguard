package io.payguard.userservice.application.payment.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessPaymentProviderWebhookService {

    private final PaymentProviderWebhookVerifier verifier;
    private final PaymentProviderWebhookParser parser;
    private final PaymentProviderWebhookDispatcher dispatcher;

    public void execute(
            ProcessPaymentProviderWebhookCommand command
    ) {

        verifier.verify(command.payload(), command.signature());
        PaymentProviderWebhook webhook = parser.parse(command.payload());
        dispatcher.dispatch(webhook);
    }
}