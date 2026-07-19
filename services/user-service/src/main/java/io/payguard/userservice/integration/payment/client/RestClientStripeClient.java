package io.payguard.userservice.integration.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountLinkRequest;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountLinkResponse;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountRequest;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountResponse;
import io.payguard.userservice.integration.payment.config.StripeProperties;
import io.payguard.userservice.integration.payment.error.dto.StripeError;
import io.payguard.userservice.integration.payment.error.dto.StripeErrorResponse;
import io.payguard.userservice.integration.payment.error.exception.StripeAccountCreationException;
import io.payguard.userservice.integration.payment.error.exception.StripeAccountDeletionException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestClientStripeClient implements StripeClient {

    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String ACCOUNT_LINKS_ENDPOINT = "/v1/account_links";

    private final RestClient stripeRestClient;
    private final StripeProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public String createAccount(StripeAccountRequest request, String idempotencyKey) {

        try {

            StripeAccountResponse response =
                    stripeRestClient
                            .post()
                            .uri(ACCOUNTS_ENDPOINT)
                            .headers(headers -> {
                                headers.setBearerAuth(
                                        properties.secretKey()
                                );
                                headers.set(
                                        "Idempotency-Key",
                                        idempotencyKey
                                );
                            })
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(buildAccountForm(request))
                            .retrieve()
                            .body(StripeAccountResponse.class);

            if (response == null
                    || response.id() == null
                    || response.id().isBlank()) {

                throw new StripeAccountCreationException(
                        "Stripe did not return an account id."
                );
            }

            return response.id();

        } catch (HttpClientErrorException ex) {

            throw new StripeAccountCreationException(
                    extractErrorMessage(ex),
                    ex
            );

        } catch (RestClientException ex) {

            throw new StripeAccountCreationException(
                    "Failed to create Stripe account.",
                    ex
            );
        }
    }

    @Override
    public String createAccountLink(StripeAccountLinkRequest request) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("account", request.account());
        form.add("refresh_url", request.refreshUrl());
        form.add("return_url", request.returnUrl());
        form.add("type", request.type());

        try {

            StripeAccountLinkResponse response =
                    stripeRestClient
                            .post()
                            .uri(ACCOUNT_LINKS_ENDPOINT)
                            .headers(headers ->
                                    headers.setBearerAuth(
                                            properties.secretKey()
                                    )
                            )
                            .contentType(
                                    MediaType.APPLICATION_FORM_URLENCODED
                            )
                            .body(form)
                            .retrieve()
                            .body(
                                    StripeAccountLinkResponse.class
                            );

            if (response == null) {
                throw new StripeAccountCreationException(
                        "Stripe returned an empty account link response."
                );
            }

            return response.url();

        } catch (RestClientException ex) {

            throw new StripeAccountCreationException(
                    "Failed to create Stripe onboarding link.",
                    ex
            );
        }
    }

    @Override public void deleteAccount(String accountId) {

        try {

            stripeRestClient
                    .delete()
                    .uri(
                            ACCOUNTS_ENDPOINT + "/{accountId}",
                            accountId
                    )
                    .headers(headers ->
                            headers.setBearerAuth(
                                    properties.secretKey()
                            )
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException ex) {

            throw new StripeAccountDeletionException(
                    extractErrorMessage(ex),
                    ex
            );

        } catch (RestClientException ex) {

            throw new StripeAccountDeletionException(
                    "Failed to delete Stripe account.",
                    ex
            );
        }
    }

    private MultiValueMap<String, String> buildAccountForm(StripeAccountRequest request) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("type", request.type());
        form.add("country", request.country());
        form.add("email", request.email());
        form.add("capabilities[transfers][requested]", "true");

        return form;
    }

    private String extractErrorMessage(HttpClientErrorException exception) {

        try {

            return Optional.ofNullable(
                            objectMapper.readValue(
                                    exception.getResponseBodyAsByteArray(),
                                    StripeErrorResponse.class
                            )
                    )
                    .map(StripeErrorResponse::error)
                    .map(StripeError::message)
                    .filter(message -> !message.isBlank())
                    .orElse(exception.getResponseBodyAsString());

        } catch (IOException ignored) {

            return exception.getResponseBodyAsString();
        }
    }

}