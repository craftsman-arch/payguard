package io.payguard.userservice.application.payment.webhook;

public record PaymentProviderWebhook(
        String eventId,
        PaymentProviderEventType type,
        PaymentProviderWebhookPayload payload
) {
}