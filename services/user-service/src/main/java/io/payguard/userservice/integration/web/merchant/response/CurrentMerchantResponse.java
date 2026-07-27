package io.payguard.userservice.integration.web.merchant.response;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.payguard.userservice.domain.merchant.MerchantStatus;
import io.payguard.userservice.domain.merchant.PaymentAccountRequiredAction;
import io.payguard.userservice.domain.merchant.PaymentAccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Current merchant profile.")
public record CurrentMerchantResponse(

        @Schema(
                description = "Unique merchant identifier.",
                example = "6f0f4fdf-4fd0-49d4-a5f2-d6e5b48d26cb"
        )
        UUID id,

        @Schema(
                description = "Merchant email address.",
                example = "merchant@example.com"
        )
        String email,

        @Schema(
                description = "Merchant legal business name.",
                example = "Merchant Ltd"
        )
        String legalName,

        @Schema(
                description = "Merchant business type.",
                example = "INDIVIDUAL"
        )
        BusinessType businessType,

        @Schema(
                description = "ISO 3166-1 alpha-2 country code.",
                example = "NL"
        )
        String country,

        @Schema(
                description = "Current merchant status.",
                example = "ACTIVE"
        )
        MerchantStatus status,

        @Schema(
                description = "Stripe Connect payment-account readiness state.",
                example = "ACTIVE"
        )
        PaymentAccountStatus paymentAccountStatus,

        @Schema(
                description = "Provider reason associated with the current payment-account restriction.",
                example = "requirements.past_due"
        )
        String paymentAccountStatusReason,

        @Schema(
                description = "Action currently required to progress the merchant payment account.",
                example = "CONTINUE_ONBOARDING"
        )
        PaymentAccountRequiredAction paymentAccountRequiredAction,

        @Schema(
                description = "Whether this merchant may currently accept marketplace payments."
        )
        boolean isReadyForPayments,

        @Schema(
                description = "Merchant creation timestamp.",
                example = "2026-07-18T10:15:30Z"
        )
        Instant createdAt,

        @Schema(
                description = "Merchant last update timestamp.",
                example = "2026-07-18T10:16:12Z"
        )
        Instant updatedAt

) {
}
