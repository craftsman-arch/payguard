package io.payguard.userservice.integration.web.common;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Validation error response.")
public record ValidationErrorResponse(

        @Schema(
                description = "Timestamp when the validation failed.",
                example = "2026-07-18T10:45:12Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code.",
                example = "400"
        )
        int status,

        @Schema(
                description = "HTTP status reason.",
                example = "Bad Request"
        )
        String error,

        @Schema(
                description = "General validation message.",
                example = "Validation failed."
        )
        String message,

        @Schema(
                description = "Request path.",
                example = "/api/v1/merchants"
        )
        String path,

        @ArraySchema(
                arraySchema = @Schema(
                        description = "List of validation errors."
                ),
                schema = @Schema(
                        implementation = ValidationError.class
                )
        )
        List<ValidationError> errors

) {
}