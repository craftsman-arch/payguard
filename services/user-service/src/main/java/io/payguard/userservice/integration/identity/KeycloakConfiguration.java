package io.payguard.userservice.integration.identity;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakConfiguration {

    @Bean
    public RestClient keycloakRestClient(
            RestClient.Builder builder,
            KeycloakProperties properties
    ) {

        return builder
                .baseUrl(properties.serverUrl())
                .build();
    }

}