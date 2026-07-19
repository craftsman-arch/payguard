package io.payguard.userservice.integration.web.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Error response.")
public record ErrorResponse(

        @Schema(
                description = "Timestamp when the error occurred.",
                example = "2026-07-18T10:45:12Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code.",
                example = "404"
        )
        int status,

        @Schema(
                description = "HTTP status reason.",
                example = "Not Found"
        )
        String error,

        @Schema(
                description = "Human-readable error message.",
                example = "Merchant not found."
        )
        String message,

        @Schema(
                description = "Request path.",
                example = "/api/v1/merchants/me"
        )
        String path

) {
}