package io.payguard.userservice.integration.web.merchant.response;

import io.payguard.userservice.domain.merchant.MerchantStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Merchant registration response.")
public record RegisterMerchantResponse(

        @Schema(
                description = "Unique merchant identifier.",
                example = "6f0f4fdf-4fd0-49d4-a5f2-d6e5b48d26cb"
        )
        UUID id,

        @Schema(
                description = "Current merchant status.",
                example = "PENDING"
        )
        MerchantStatus status,

        @Schema(
                description = """
                        Stripe Connect onboarding URL.

                        Redirect the merchant to this URL to complete
                        Stripe Connect onboarding and identity verification.
                        """,
                example = "https://connect.stripe.com/setup/s/acct_1234567890"
        )
        String onboardingUrl

) {
}