package io.payguard.userservice.integration.payment.events.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.payguard.userservice.application.payment.events.StripeEventType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StripeEvent(

        String id,
        StripeEventType type,
        StripeEventData data

) {
}