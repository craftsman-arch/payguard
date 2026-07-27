package io.payguard.userservice.integration.payment.account.dto;

public record StripeAccountRequest(

        String type,
        String country,
        String email,
        String businessType
) {
}
