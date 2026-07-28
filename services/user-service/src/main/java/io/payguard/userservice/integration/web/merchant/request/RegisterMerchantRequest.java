package io.payguard.userservice.integration.web.merchant.request;

import io.payguard.userservice.domain.merchant.BusinessType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Merchant registration request.")
public record RegisterMerchantRequest(

        @Schema(
                description = "Merchant email address.",
                example = "merchant@example.com"
        )
        @Email
        @NotBlank
        String email,

        @Schema(
                description = "Merchant-selected password.",
                format = "password",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank
        @Size(max = 128)
        String password,

        @Schema(
                description = "Merchant legal business name.",
                example = "Merchant Ltd"
        )
        @NotBlank
        String legalName,

        @Schema(
                description = "Merchant business type.",
                example = "INDIVIDUAL"
        )
        @NotNull
        BusinessType businessType,

        @Schema(
                description = "ISO 3166-1 alpha-2 country code.",
                example = "NL"
        )
        @Pattern(regexp = "^[A-Za-z]{2}$")
        @NotBlank
        String country

) {

    @Override
    public String toString() {

        return "RegisterMerchantRequest[" +
                "email=" + email +
                ", password=[REDACTED]" +
                ", legalName=" + legalName +
                ", businessType=" + businessType +
                ", country=" + country +
                ']';
    }
}
