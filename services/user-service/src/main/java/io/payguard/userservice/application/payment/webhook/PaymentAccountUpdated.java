package io.payguard.userservice.application.payment.webhook;

import io.payguard.userservice.domain.merchant.PaymentAccountRequirements;

import java.time.Instant;

import static java.util.Optional.ofNullable;

public record PaymentAccountUpdated(

        String paymentAccountId,
        boolean payoutsEnabled,
        boolean cardPaymentsCapabilityActive,
        boolean transfersCapabilityActive,
        String disabledReason,
        PaymentAccountRequirements requirements,
        Instant eventAt

) implements PaymentProviderWebhookPayload {

    public PaymentAccountUpdated {

        requirements = ofNullable(requirements)
                .orElseGet(PaymentAccountRequirements::empty);
    }
}