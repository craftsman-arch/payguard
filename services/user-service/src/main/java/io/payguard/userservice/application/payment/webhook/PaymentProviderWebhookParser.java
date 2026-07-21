package io.payguard.userservice.application.payment.webhook;

public interface PaymentProviderWebhookParser {
    PaymentProviderWebhook parse(String payload);
}