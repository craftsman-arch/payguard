package io.payguard.userservice.integration.payment.events.dto;

public record StripeSignature(

        long timestamp,
        String signature

) {
}