package io.payguard.userservice.integration.payment.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StripeAccountLinkResponse(

        String url,

        @JsonProperty("expires_at")
        long expiresAt

) {
}