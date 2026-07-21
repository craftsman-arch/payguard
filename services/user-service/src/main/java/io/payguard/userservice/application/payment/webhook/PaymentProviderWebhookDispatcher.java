package io.payguard.userservice.application.payment.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProviderWebhookDispatcher {

    private final List<PaymentProviderWebhookHandler<?>> handlers;

    public void dispatch(PaymentProviderWebhook webhook) {

        handlers.stream()
                .filter(handler ->
                        handler.supports() == webhook.type()
                )
                .findFirst()
                .ifPresentOrElse(

                        handler -> dispatchToHandler(
                                handler,
                                webhook
                        ),

                        () -> log.info(
                                "Ignoring unsupported payment-provider webhook [{}] with event id [{}].",
                                webhook.type(),
                                webhook.eventId()
                        )
                );
    }

    private <T extends PaymentProviderWebhookPayload> void dispatchToHandler(
            PaymentProviderWebhookHandler<T> handler,
            PaymentProviderWebhook webhook
    ) {

        PaymentProviderWebhookPayload payload = webhook.payload();

        if (!handler.payloadType().isInstance(payload)) {

            log.error(
                    "Webhook payload type mismatch for event [{}], event id [{}]. Expected [{}], received [{}].",
                    webhook.type(),
                    webhook.eventId(),
                    handler.payloadType().getSimpleName(),
                    payload.getClass().getSimpleName()
            );

            return;
        }

        T typedPayload = handler.payloadType().cast(
                payload
        );

        handler.handle(
                typedPayload
        );
    }
}