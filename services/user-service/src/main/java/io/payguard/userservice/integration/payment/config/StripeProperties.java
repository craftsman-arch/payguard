package io.payguard.userservice.integration.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(

        String baseUrl,
        String secretKey,
        String webhookSecret,
        String refreshUrl,
        String returnUrl

) {
}