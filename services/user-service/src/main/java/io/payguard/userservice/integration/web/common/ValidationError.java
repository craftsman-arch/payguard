package io.payguard.userservice.integration.web.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error.")
public record ValidationError(

        @Schema(
                description = "Invalid field.",
                example = "email"
        )
        String field,

        @Schema(
                description = "Validation error message.",
                example = "must be a well-formed email address"
        )
        String message

) {
}