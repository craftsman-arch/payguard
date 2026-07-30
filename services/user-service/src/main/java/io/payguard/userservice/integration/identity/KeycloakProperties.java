package io.payguard.userservice.integration.identity;

import io.payguard.userservice.integration.http.HttpClientTimeouts;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(

        String serverUrl,
        String realm,
        String clientId,
        String clientSecret,
        HttpClientTimeouts httpClient
) {
}
