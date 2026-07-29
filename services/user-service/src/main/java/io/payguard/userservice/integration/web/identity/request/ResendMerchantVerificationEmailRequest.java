package io.payguard.userservice.integration.web.identity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Merchant verification-email resend request.")
public record ResendMerchantVerificationEmailRequest(

        @Schema(
                description = "Merchant email address.",
                example = "merchant@example.com"
        )
        @Email
        @NotBlank
        String email

) {
}
