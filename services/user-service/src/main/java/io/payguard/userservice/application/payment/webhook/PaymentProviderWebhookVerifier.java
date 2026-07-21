package io.payguard.userservice.application.payment.webhook;

public interface PaymentProviderWebhookVerifier {
    void verify(String payload, String signature);
}