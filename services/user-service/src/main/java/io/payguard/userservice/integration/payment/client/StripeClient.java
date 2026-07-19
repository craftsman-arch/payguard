package io.payguard.userservice.integration.payment.client;

import io.payguard.userservice.integration.payment.account.dto.StripeAccountLinkRequest;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountRequest;

public interface StripeClient {

    String createAccount(
            StripeAccountRequest request,
            String idempotencyKey
    );

    String createAccountLink(
            StripeAccountLinkRequest request
    );

    void deleteAccount(
            String accountId
    );

}