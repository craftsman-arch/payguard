package io.payguard.userservice.integration.identity.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.payguard.userservice.integration.identity.KeycloakProperties;
import io.payguard.userservice.integration.identity.KeycloakRoleRepresentation;
import io.payguard.userservice.integration.identity.dto.KeycloakCreateUserRequest;
import io.payguard.userservice.integration.identity.dto.KeycloakErrorResponse;
import io.payguard.userservice.integration.identity.dto.KeycloakUser;
import io.payguard.userservice.integration.identity.exception.KeycloakPasswordPolicyException;
import io.payguard.userservice.integration.identity.exception.KeycloakRoleAssignmentException;
import io.payguard.userservice.integration.identity.exception.KeycloakUnavailableException;
import io.payguard.userservice.integration.identity.exception.KeycloakUserConflictException;
import io.payguard.userservice.integration.identity.exception.KeycloakUserCreationException;
import io.payguard.userservice.integration.identity.exception.KeycloakVerificationEmailException;
import io.payguard.userservice.integration.identity.token.KeycloakTokenClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static io.payguard.userservice.integration.http.TransientHttpStatusClassifier.isTransient;

@Component
@RequiredArgsConstructor
public class RestClientKeycloakAdminClient implements KeycloakAdminClient {

    private static final String USERS_ENDPOINT =
            "/admin/realms/{realm}/users";

    private static final String ROLE_ENDPOINT =
            "/admin/realms/{realm}/roles/{roleName}";

    private static final String USER_REALM_ROLE_MAPPING_ENDPOINT =
            "/admin/realms/{realm}/users/{userId}/role-mappings/realm";

    private static final String SEND_VERIFICATION_EMAIL_ENDPOINT =
            "/admin/realms/{realm}/users/{userId}/send-verify-email";

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;
    private final KeycloakTokenClient tokenClient;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<KeycloakUser> findByEmail(String email) {

        try {

            KeycloakUser[] users = keycloakRestClient
                    .get()
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path(USERS_ENDPOINT)
                                    .queryParam("email", email)
                                    .queryParam("exact", true)
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

        } catch (HttpClientErrorException ex) {

            if (isTransient(ex.getStatusCode())) {
                throw new KeycloakUnavailableException(
                        "Keycloak is temporarily unavailable while searching for a user.",
                        ex
                );
            }

            throw new KeycloakUserCreationException(
                    "Keycloak rejected the user search request.",
                    ex
            );

        } catch (HttpServerErrorException | ResourceAccessException ex) {

            throw new KeycloakUnavailableException(
                    "Keycloak is unavailable while searching for a user.",
                    ex
            );

        } catch (RestClientException ex) {

            throw new KeycloakUserCreationException(
                    "Unable to process Keycloak user-search response.",
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

        } catch (HttpClientErrorException.BadRequest ex) {

            KeycloakErrorResponse error = deserializeError(ex);

            if (error.isPasswordPolicyViolation()) {
                throw new KeycloakPasswordPolicyException(
                        "Keycloak rejected the password.",
                        ex
                );
            }

            throw new KeycloakUserCreationException(
                    "Keycloak rejected the user creation request.",
                    ex
            );

        } catch (HttpClientErrorException.Conflict ex) {

            throw new KeycloakUserConflictException(
                    "A Keycloak user with the requested identity already exists.",
                    ex
            );

        } catch (HttpClientErrorException ex) {

            if (isTransient(ex.getStatusCode())) {
                throw new KeycloakUnavailableException(
                        "Keycloak is temporarily unavailable.",
                        ex
                );
            }

            throw new KeycloakUserCreationException(
                    "Keycloak rejected the user creation request.",
                    ex
            );

        } catch (HttpServerErrorException | ResourceAccessException ex) {

            throw new KeycloakUnavailableException(
                    "Keycloak is unavailable.",
                    ex
            );

        } catch (RestClientException ex) {

            throw new KeycloakUserCreationException(
                    "Failed to create user in Keycloak.",
                    ex
            );
        }
    }

    private KeycloakErrorResponse deserializeError(
            HttpClientErrorException exception
    ) {

        try {
            return objectMapper.readValue(
                    exception.getResponseBodyAsByteArray(),
                    KeycloakErrorResponse.class
            );

        } catch (IOException deserializationException) {

            exception.addSuppressed(deserializationException);

            return new KeycloakErrorResponse(null, null);
        }
    }

    @Override
    public void assignRealmRole(String userId, String roleName) {

        try {

            KeycloakRoleRepresentation role = findRealmRole(roleName);
            keycloakRestClient
                    .post()
                    .uri(
                            USER_REALM_ROLE_MAPPING_ENDPOINT,
                            properties.realm(),
                            userId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(
                                    tokenClient.getAccessToken()
                            )
                    )
                    .body(List.of(role))
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException ex) {

            if (isTransient(ex.getStatusCode())) {
                throw new KeycloakUnavailableException(
                        "Keycloak is temporarily unavailable while assigning a realm role.",
                        ex
                );
            }

            throw new KeycloakRoleAssignmentException(
                    "Keycloak rejected realm role assignment.",
                    ex
            );

        } catch (HttpServerErrorException | ResourceAccessException ex) {

            throw new KeycloakUnavailableException(
                    "Keycloak is unavailable while assigning a realm role.",
                    ex
            );

        } catch (RestClientException ex) {

            throw new KeycloakRoleAssignmentException(
                    "Failed to assign realm role '%s'."
                            .formatted(roleName),
                    ex
            );
        }
    }

    private KeycloakRoleRepresentation findRealmRole(String roleName) {

        KeycloakRoleRepresentation role =
                keycloakRestClient
                        .get()
                        .uri(
                                ROLE_ENDPOINT,
                                properties.realm(),
                                roleName
                        )
                        .headers(headers ->
                                headers.setBearerAuth(
                                        tokenClient.getAccessToken()
                                )
                        )
                        .retrieve()
                        .body(KeycloakRoleRepresentation.class);

        if (role == null) {
            throw new KeycloakRoleAssignmentException(
                    "Realm role '%s' was not found."
                            .formatted(roleName)
            );
        }

        return role;
    }

    @Override
    public void sendVerificationEmail(String userId) {

        try {

            keycloakRestClient
                    .put()
                    .uri(
                            SEND_VERIFICATION_EMAIL_ENDPOINT,
                            properties.realm(),
                            userId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(
                                    tokenClient.getAccessToken()
                            )
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException ex) {

            if (isTransient(ex.getStatusCode())) {
                throw new KeycloakUnavailableException(
                        "Keycloak is temporarily unavailable while sending verification email.",
                        ex
                );
            }

            throw new KeycloakVerificationEmailException(
                    "Keycloak rejected the verification-email request.",
                    ex
            );

        } catch (HttpServerErrorException | ResourceAccessException ex) {

            throw new KeycloakUnavailableException(
                    "Keycloak is unavailable while sending verification email.",
                    ex
            );

        } catch (RestClientException ex) {

            throw new KeycloakVerificationEmailException(
                    "Failed to send Keycloak verification email.",
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
