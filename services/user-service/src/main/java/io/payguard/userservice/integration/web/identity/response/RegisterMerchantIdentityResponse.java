package io.payguard.userservice.integration.web.identity.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Merchant identity registration result.")
public record RegisterMerchantIdentityResponse(

        @Schema(
                description = "Next registration state.",
                example = "PENDING_VERIFICATION"
        )
        String status

) {

    public static RegisterMerchantIdentityResponse pendingVerification() {

        return new RegisterMerchantIdentityResponse(
                "PENDING_VERIFICATION"
        );
    }
}
