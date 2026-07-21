package io.payguard.userservice.integration.payment.stripe.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StripeAccountDto(

        String id,

        @JsonProperty("charges_enabled")
        boolean chargesEnabled,

        @JsonProperty("payouts_enabled")
        boolean payoutsEnabled
) {
}