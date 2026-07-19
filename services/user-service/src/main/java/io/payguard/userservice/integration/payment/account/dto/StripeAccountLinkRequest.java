package io.payguard.userservice.integration.payment.account.dto;

public record StripeAccountLinkRequest(

        String account,
        String refreshUrl,
        String returnUrl,
        String type

) {
}