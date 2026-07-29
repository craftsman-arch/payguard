package io.payguard.userservice.integration.web.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecoverOutboxEventRequest(

        @NotBlank(message = "Recovery reason is required.")
        @Size(
                max = 1000,
                message = "Recovery reason must not exceed 1000 characters."
        )
        String reason

) {
}
