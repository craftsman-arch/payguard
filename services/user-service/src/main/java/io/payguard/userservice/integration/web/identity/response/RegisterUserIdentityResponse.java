package io.payguard.userservice.integration.web.identity.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User identity registration result.")
public record RegisterUserIdentityResponse(

        @Schema(
                description = "Next registration state.",
                example = "PENDING_VERIFICATION"
        )
        String status

) {

    public static RegisterUserIdentityResponse pendingVerification() {

        return new RegisterUserIdentityResponse(
                "PENDING_VERIFICATION"
        );
    }
}
