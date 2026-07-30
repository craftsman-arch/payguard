package io.payguard.userservice.integration.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakConfiguration {

    @Bean
    public RestClient keycloakRestClient(RestClient.Builder builder, KeycloakProperties properties) {

        log.info("Keycloak URL: {}", properties.serverUrl());

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.httpClient().connectTimeout());
        requestFactory.setReadTimeout(properties.httpClient().readTimeout());

        return builder
                .baseUrl(properties.serverUrl())
                .requestFactory(requestFactory)
                .build();
    }

}
