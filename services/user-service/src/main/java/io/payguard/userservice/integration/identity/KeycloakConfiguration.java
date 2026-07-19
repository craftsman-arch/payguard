package io.payguard.userservice.integration.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakConfiguration {

    @Bean
    public RestClient keycloakRestClient(RestClient.Builder builder, KeycloakProperties properties) {

        log.info("Keycloak URL: {}", properties.serverUrl());
        return builder
                .baseUrl(properties.serverUrl())
                .build();
    }

}