package io.payguard.userservice.integration.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfiguration {

    @Bean
    RestClient stripeRestClient(RestClient.Builder builder, StripeProperties properties) {

        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }

}