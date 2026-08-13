package io.payguard.userservice.integration.web.user;

import io.payguard.userservice.application.user.profile.CreateUserProfileCommand;
import io.payguard.userservice.application.user.profile.CreateUserProfileResult;
import io.payguard.userservice.application.user.profile.CreateUserProfileService;
import io.payguard.userservice.domain.common.value.Country;
import io.payguard.userservice.domain.settlement.value.AccountHolderName;
import io.payguard.userservice.domain.user.value.DisplayName;
import io.payguard.userservice.integration.web.common.ErrorResponse;
import io.payguard.userservice.integration.web.common.ValidationErrorResponse;
import io.payguard.userservice.integration.web.user.request.CreateUserProfileRequest;
import io.payguard.userservice.integration.web.user.response.CreateUserProfileResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "PayGuard user profile management.")
public class UserController {

    private final CreateUserProfileService createUserProfileService;

    @Operation(
            summary = "Create user profile",
            description = """
                    Creates a User and an unlinked settlement account for the
                    authenticated, email-verified identity. No external payment
                    provider is called by this operation. Repeated requests
                    return the existing User.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created."),
            @ApiResponse(responseCode = "200", description = "Existing profile returned."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(schema = @Schema(
                            implementation = ValidationErrorResponse.class
                    ))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required."),
            @ApiResponse(
                    responseCode = "403",
                    description = "USER role or verified email is missing.",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class
                    ))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreateUserProfileResponse> createProfile(
            @Valid @RequestBody CreateUserProfileRequest request
    ) {
        CreateUserProfileResult result = createUserProfileService.execute(
                new CreateUserProfileCommand(
                        DisplayName.of(request.displayName()),
                        AccountHolderName.of(request.accountHolderName()),
                        request.accountHolderType(),
                        Country.of(request.country())
                )
        );

        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(new CreateUserProfileResponse(
                        result.id(),
                        result.status()
                ));
    }
}
