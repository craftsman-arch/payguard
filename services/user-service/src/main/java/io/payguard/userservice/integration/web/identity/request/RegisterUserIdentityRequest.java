package io.payguard.userservice.integration.web.identity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User identity registration request.")
public record RegisterUserIdentityRequest(

        @Schema(
                description = "User email address.",
                example = "user@example.com"
        )
        @Email
        @NotBlank
        String email,

        @Schema(
                description = "User-selected permanent password.",
                format = "password",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank
        @Size(max = 128)
        String password

) {

    @Override
    public String toString() {

        return "RegisterUserIdentityRequest[" +
                "email=" + email +
                ", password=[REDACTED]" +
                ']';
    }
}
