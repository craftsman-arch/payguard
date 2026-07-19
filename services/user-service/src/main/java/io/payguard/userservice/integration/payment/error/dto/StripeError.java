package io.payguard.userservice.integration.payment.error.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StripeError(

        String type,
        String code,
        String message
) {
}