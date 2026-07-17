package io.payguard.userservice.integration.identity.client;

import io.payguard.userservice.integration.identity.KeycloakProperties;
import io.payguard.userservice.integration.identity.dto.KeycloakCreateUserRequest;
import io.payguard.userservice.integration.identity.dto.KeycloakUser;
import io.payguard.userservice.integration.identity.exception.KeycloakUserCreationException;
import io.payguard.userservice.integration.identity.exception.KeycloakUserDeletionException;
import io.payguard.userservice.integration.identity.token.KeycloakTokenClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestClientKeycloakAdminClient implements KeycloakAdminClient {

    private static final String USERS_ENDPOINT =
            "/admin/realms/{realm}/users";

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;
    private final KeycloakTokenClient tokenClient;

    @Override
    public Optional<KeycloakUser> findByEmail(String email) {

        try {

            KeycloakUser[] users = keycloakRestClient
                    .get()
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path(USERS_ENDPOINT)
                                    .queryParam("email", email)
                                    .build(properties.realm())
                    )
                    .headers(headers ->
                            headers.setBearerAuth(tokenClient.getAccessToken())
                    )
                    .retrieve()
                    .body(KeycloakUser[].class);

            if (users == null || users.length == 0) {
                return Optional.empty();
            }

            if (users.length > 1) {
                throw new KeycloakUserCreationException(
                        "Multiple Keycloak users found for email '%s'."
                                .formatted(email)
                );
            }

            return Optional.of(users[0]);

        } catch (RestClientException ex) {

            throw new KeycloakUserCreationException(
                    "Failed to search Keycloak user by email.",
                    ex
            );
        }
    }

    @Override
    public String createUser(KeycloakCreateUserRequest request) {

        try {

            ResponseEntity<Void> response = keycloakRestClient
                    .post()
                    .uri(USERS_ENDPOINT, properties.realm())
                    .headers(headers ->
                            headers.setBearerAuth(tokenClient.getAccessToken())
                    )
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            URI location = response.getHeaders().getLocation();

            if (location == null) {
                throw new KeycloakUserCreationException(
                        "Keycloak did not return Location header."
                );
            }

            return extractUserId(location);

        } catch (HttpClientErrorException.Conflict ex) {

            return findByEmail(request.email())
                    .map(KeycloakUser::id)
                    .orElseThrow(() ->
                            new KeycloakUserCreationException(
                                    "Keycloak returned 409 Conflict, but no user was found for email: "
                                            + request.email(),
                                    ex
                            )
                    );

        } catch (RestClientException ex) {

            throw new KeycloakUserCreationException(
                    "Failed to create user in Keycloak.",
                    ex
            );
        }
    }

    @Override
    public void deleteUser(String userId) {

        try {

            keycloakRestClient
                    .delete()
                    .uri(
                            USERS_ENDPOINT + "/{userId}",
                            properties.realm(),
                            userId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(tokenClient.getAccessToken())
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException ex) {

            throw new KeycloakUserDeletionException(
                    "Failed to delete user from Keycloak.",
                    ex
            );
        }
    }

    private String extractUserId(URI location) {

        String path = location.getPath();

        int index = path.lastIndexOf('/');

        if (index < 0 || index == path.length() - 1) {
            throw new KeycloakUserCreationException(
                    "Unable to extract user id from Location header."
            );
        }

        return path.substring(index + 1);
    }

}