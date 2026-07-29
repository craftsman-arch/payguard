package io.payguard.userservice.integration.web.identity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Merchant identity registration request.")
public record RegisterMerchantIdentityRequest(

        @Schema(
                description = "Merchant email address.",
                example = "merchant@example.com"
        )
        @Email
        @NotBlank
        String email,

        @Schema(
                description = "Merchant-selected permanent password.",
                format = "password",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank
        @Size(max = 128)
        String password

) {

    @Override
    public String toString() {

        return "RegisterMerchantIdentityRequest[" +
                "email=" + email +
                ", password=[REDACTED]" +
                ']';
    }
}
