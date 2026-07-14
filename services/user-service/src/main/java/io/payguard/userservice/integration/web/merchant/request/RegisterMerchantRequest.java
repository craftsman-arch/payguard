package io.payguard.userservice.integration.web.merchant.request;

import io.payguard.userservice.domain.merchant.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterMerchantRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        String legalName,

        @NotNull
        BusinessType businessType,

        @Pattern(regexp = "^[A-Za-z]{2}$")
        @NotBlank
        String country

) {
}