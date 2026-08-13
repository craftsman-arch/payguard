package io.payguard.userservice.integration.web.user.request;

import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Authenticated user profile creation request.")
public record CreateUserProfileRequest(
        @NotBlank
        @Schema(example = "Alex")
        String displayName,

        @NotBlank
        @Schema(example = "Alex Smith")
        String accountHolderName,

        @NotNull
        @Schema(example = "INDIVIDUAL")
        SettlementAccountHolderType accountHolderType,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{2}$")
        @Schema(example = "GB")
        String country
) {
}
