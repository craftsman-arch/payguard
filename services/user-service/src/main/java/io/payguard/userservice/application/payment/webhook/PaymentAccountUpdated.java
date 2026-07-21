package io.payguard.userservice.application.payment.webhook;

public record PaymentAccountUpdated (
        String paymentAccountId,
        boolean chargesEnabled,
        boolean payoutsEnabled
) implements PaymentProviderWebhookPayload {
    public boolean isFullyEnabled() {
        return chargesEnabled && payoutsEnabled;
    }
}