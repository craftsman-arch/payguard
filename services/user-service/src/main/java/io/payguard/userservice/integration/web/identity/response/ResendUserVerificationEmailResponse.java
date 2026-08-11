package io.payguard.userservice.integration.web.identity.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Verification-email resend result.")
public record ResendUserVerificationEmailResponse(

        @Schema(example = "VERIFICATION_EMAIL_REQUESTED")
        String status

) {

    public static ResendUserVerificationEmailResponse requested() {
        return new ResendUserVerificationEmailResponse(
                "VERIFICATION_EMAIL_REQUESTED"
        );
    }
}
