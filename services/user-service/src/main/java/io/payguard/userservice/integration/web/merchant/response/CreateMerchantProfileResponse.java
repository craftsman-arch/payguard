package io.payguard.userservice.integration.web.merchant.response;

import io.payguard.userservice.domain.merchant.MerchantStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Merchant profile creation result.")
public record CreateMerchantProfileResponse(

        @Schema(description = "Unique merchant identifier.")
        UUID id,

        @Schema(description = "Current merchant status.")
        MerchantStatus status

) {
}
