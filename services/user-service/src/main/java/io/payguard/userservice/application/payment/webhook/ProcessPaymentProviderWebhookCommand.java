package io.payguard.userservice.application.payment.webhook;

import lombok.NonNull;

public record ProcessPaymentProviderWebhookCommand(

        @NonNull String payload,
        @NonNull String signature
) {
}