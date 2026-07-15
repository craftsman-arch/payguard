package io.payguard.userservice.integration.identity.token;

import io.payguard.userservice.integration.identity.KeycloakProperties;
import io.payguard.userservice.integration.identity.exception.KeycloakAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class RestClientKeycloakTokenClient implements KeycloakTokenClient {

    private static final String TOKEN_ENDPOINT =
            "/realms/{realm}/protocol/openid-connect/token";

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;

    @Override
    public String getAccessToken() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        try {

            KeycloakTokenResponse response = keycloakRestClient
                    .post()
                    .uri(TOKEN_ENDPOINT, properties.realm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new KeycloakAuthenticationException(
                        "Keycloak did not return an access token."
                );
            }

            return response.accessToken();

        } catch (RestClientException ex) {

            throw new KeycloakAuthenticationException(
                    "Failed to obtain access token from Keycloak.",
                    ex
            );
        }
    }

}