package io.payguard.userservice.integration.web.event;

import io.payguard.userservice.application.event.recovery.RecoverOutboxEventCommand;
import io.payguard.userservice.application.event.recovery.RecoverOutboxEventResult;
import io.payguard.userservice.application.event.recovery.RecoverOutboxEventService;
import io.payguard.userservice.integration.web.common.ErrorResponse;
import io.payguard.userservice.integration.web.common.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/outbox/events")
@RequiredArgsConstructor
@Tag(
        name = "Outbox Operations",
        description = "Administrative recovery operations for transactional outbox events."
)
public class AdminOutboxEventController {

    private static final String RECOVERY_SCHEDULED = "RECOVERY_SCHEDULED";

    private final RecoverOutboxEventService recoverOutboxEventService;

    @Operation(
            summary = "Recover an exhausted outbox event",
            description = """
                    Returns the original exhausted event to the publication
                    pipeline. The event keeps its original identity, payload
                    and aggregate ordering position. This operation does not
                    publish directly to Kafka.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "The original event was scheduled for another publication cycle.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = RecoverOutboxEventResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The recovery request is invalid.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or invalid."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user does not have the ADMIN role.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The outbox event does not exist.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The event is not exhausted or has already been published.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/{eventId}/recovery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecoverOutboxEventResponse> recover(
            @PathVariable UUID eventId,
            @Valid @RequestBody
            RecoverOutboxEventRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        RecoverOutboxEventResult result =
                recoverOutboxEventService.execute(
                        new RecoverOutboxEventCommand(
                                eventId,
                                jwt.getSubject(),
                                request.reason()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        new RecoverOutboxEventResponse(
                                result.recoveryId(),
                                result.eventId(),
                                RECOVERY_SCHEDULED
                        )
                );
    }
}
