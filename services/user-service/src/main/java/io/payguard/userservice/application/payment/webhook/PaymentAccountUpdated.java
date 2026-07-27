package io.payguard.userservice.application.payment.webhook;

import java.time.Instant;

public record PaymentAccountUpdated(
        String paymentAccountId,
        boolean payoutsEnabled,
        boolean cardPaymentsCapabilityActive,
        boolean transfersCapabilityActive,
        String disabledReason,
        Instant eventAt
) implements PaymentProviderWebhookPayload { }
