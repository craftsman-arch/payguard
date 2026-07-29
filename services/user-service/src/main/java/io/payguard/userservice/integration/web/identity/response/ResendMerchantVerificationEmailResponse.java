package io.payguard.userservice.integration.web.identity.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Verification-email resend result.")
public record ResendMerchantVerificationEmailResponse(

        @Schema(example = "VERIFICATION_EMAIL_REQUESTED")
        String status

) {

    public static ResendMerchantVerificationEmailResponse requested() {
        return new ResendMerchantVerificationEmailResponse(
                "VERIFICATION_EMAIL_REQUESTED"
        );
    }
}
