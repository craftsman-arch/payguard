package io.payguard.userservice.integration.payment.events.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StripeAccount(

        String id,

        @JsonProperty("charges_enabled")
        boolean chargesEnabled,

        @JsonProperty("payouts_enabled")
        boolean payoutsEnabled

) {

    public boolean isFullyEnabled() {
        return chargesEnabled && payoutsEnabled;
    }
}