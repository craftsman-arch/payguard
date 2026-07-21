package io.payguard.userservice.application.payment.webhook;

public interface PaymentProviderWebhookHandler<T extends PaymentProviderWebhookPayload> {

    PaymentProviderEventType supports();

    Class<T> payloadType();

    void handle(T payload);
}