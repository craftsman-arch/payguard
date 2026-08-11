package io.payguard.userservice.integration.web.identity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User verification-email resend request.")
public record ResendUserVerificationEmailRequest(

        @Schema(
                description = "User email address.",
                example = "user@example.com"
        )
        @Email
        @NotBlank
        String email

) {
}
