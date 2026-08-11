package io.payguard.userservice.integration.web.identity;

import io.payguard.userservice.application.identity.registration.RegisterUserIdentityCommand;
import io.payguard.userservice.application.identity.registration.RegisterUserIdentityService;
import io.payguard.userservice.application.identity.registration.ResendUserVerificationEmailCommand;
import io.payguard.userservice.application.identity.registration.ResendUserVerificationEmailService;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.web.common.ErrorResponse;
import io.payguard.userservice.integration.web.common.ValidationErrorResponse;
import io.payguard.userservice.integration.web.identity.request.RegisterUserIdentityRequest;
import io.payguard.userservice.integration.web.identity.request.ResendUserVerificationEmailRequest;
import io.payguard.userservice.integration.web.identity.response.RegisterUserIdentityResponse;
import io.payguard.userservice.integration.web.identity.response.ResendUserVerificationEmailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-registrations")
@RequiredArgsConstructor
@Tag(
        name = "User registration",
        description = "Public PayGuard user identity registration."
)
public class UserIdentityRegistrationController {

    private final RegisterUserIdentityService registrationService;
    private final ResendUserVerificationEmailService resendVerificationEmailService;

    @Operation(
            summary = "Register user identity",
            description = """
                    Creates a Keycloak identity with a permanent password,
                    assigns the USER role and starts email verification
                    and OTP configuration.

                    This operation does not create a PayGuard user profile or
                    settlement account and does not issue authentication tokens.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Identity registered; verification is required.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            RegisterUserIdentityResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Identity already exists.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Password violates the security policy.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Identity provider returned an unexpected response.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Identity provider is temporarily unavailable.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RegisterUserIdentityResponse register(
            @Valid @RequestBody RegisterUserIdentityRequest request
    ) {

        registrationService.execute(
                new RegisterUserIdentityCommand(
                        Email.of(request.email()),
                        request.password()
                )
        );

        return RegisterUserIdentityResponse.pendingVerification();
    }

    @Operation(
            summary = "Resend user verification email",
            description = """
                    Requests another verification email. The response is
                    intentionally identical whether or not the email belongs
                    to an existing unverified identity.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Verification-email request accepted.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            ResendUserVerificationEmailResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Identity provider or email delivery is temporarily unavailable.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/verification-email")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResendUserVerificationEmailResponse resendVerificationEmail(
            @Valid @RequestBody
            ResendUserVerificationEmailRequest request
    ) {

        resendVerificationEmailService.execute(
                new ResendUserVerificationEmailCommand(
                        Email.of(request.email())
                )
        );

        return ResendUserVerificationEmailResponse.requested();
    }
}
