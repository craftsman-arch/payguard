package io.payguard.userservice.integration.payment.config;

import io.payguard.userservice.integration.http.HttpClientTimeouts;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(

        String baseUrl,
        String secretKey,
        String webhookSecret,
        String refreshUrl,
        String returnUrl,
        HttpClientTimeouts httpClient

) {
}
