package io.payguard.userservice.application.payment.webhook;

import java.time.Instant;

public interface ProcessedPaymentProviderEventRepository {

    boolean tryClaim(
            String eventId,
            PaymentProviderEventType eventType,
            Instant processedAt
    );
}