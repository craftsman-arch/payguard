package io.payguard.userservice.integration.payment.provider;

import io.payguard.userservice.application.payment.SettlementAccountOnboardingRequest;
import io.payguard.userservice.application.payment.SettlementAccountProvisioningRequest;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountLinkRequest;
import io.payguard.userservice.integration.payment.account.dto.StripeAccountRequest;
import io.payguard.userservice.integration.payment.account.mapper.StripeAccountMapper;
import io.payguard.userservice.integration.payment.client.StripeClient;
import io.payguard.userservice.integration.payment.config.StripeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StripePaymentProviderTests {

    private static final String REFRESH_URL =
            "http://localhost:5173/onboarding/retry";

    private static final String RETURN_URL =
            "http://localhost:5173/onboarding/success";

    @Mock
    private StripeClient stripeClient;

    private StripePaymentProvider paymentProvider;

    @BeforeEach
    void setUp() {
        StripeProperties properties = new StripeProperties(
                null,
                null,
                null,
                REFRESH_URL,
                RETURN_URL,
                null
        );

        paymentProvider = new StripePaymentProvider(
                stripeClient,
                new StripeAccountMapper(properties)
        );
    }

    @Test
    void provisionsStripeAccountFromProviderNeutralRequest() {
        String idempotencyKey = "settlement-account:account-id";
        SettlementAccountProvisioningRequest request =
                new SettlementAccountProvisioningRequest(
                        idempotencyKey,
                        "user@test.com",
                        "GB",
                        "INDIVIDUAL"
                );

        ArgumentCaptor<StripeAccountRequest> requestCaptor = ArgumentCaptor.forClass(StripeAccountRequest.class);

        given(stripeClient.createAccount(requestCaptor.capture(), ArgumentMatchers.eq(idempotencyKey)))
                .willReturn("acct_123");

        String providerAccountId = paymentProvider.createSettlementAccount(request);

        StripeAccountRequest stripeRequest = requestCaptor.getValue();

        assertThat(providerAccountId).isEqualTo("acct_123");
        assertThat(stripeRequest.type()).isEqualTo("express");
        assertThat(stripeRequest.country()).isEqualTo("GB");
        assertThat(stripeRequest.email()).isEqualTo("user@test.com");
        assertThat(stripeRequest.businessType()).isEqualTo("individual");
    }

    @Test
    void createsOnboardingLinkForExistingProviderAccount() {
        SettlementAccountOnboardingRequest request =
                new SettlementAccountOnboardingRequest("acct_123");

        ArgumentCaptor<StripeAccountLinkRequest> requestCaptor =
                ArgumentCaptor.forClass(StripeAccountLinkRequest.class);

        given(stripeClient.createAccountLink(requestCaptor.capture()))
                .willReturn("https://connect.stripe.test/onboarding");

        String onboardingUrl = paymentProvider
                .createSettlementAccountOnboardingLink(request);

        StripeAccountLinkRequest stripeRequest = requestCaptor.getValue();

        assertThat(onboardingUrl)
                .isEqualTo("https://connect.stripe.test/onboarding");
        assertThat(stripeRequest.account()).isEqualTo("acct_123");
        assertThat(stripeRequest.refreshUrl()).isEqualTo(REFRESH_URL);
        assertThat(stripeRequest.returnUrl()).isEqualTo(RETURN_URL);
        assertThat(stripeRequest.type()).isEqualTo("account_onboarding");

        verify(stripeClient).createAccountLink(stripeRequest);
    }
}
