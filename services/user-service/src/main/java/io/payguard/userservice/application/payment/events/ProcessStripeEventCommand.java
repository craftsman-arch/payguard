package io.payguard.userservice.application.payment.events;

public record ProcessStripeEventCommand(

        String payload,
        String signature
) {
}