package io.payguard.userservice.integration.web.identity;

import io.payguard.userservice.application.identity.registration.RegisterMerchantIdentityCommand;
import io.payguard.userservice.application.identity.registration.RegisterMerchantIdentityService;
import io.payguard.userservice.application.identity.registration.ResendMerchantVerificationEmailCommand;
import io.payguard.userservice.application.identity.registration.ResendMerchantVerificationEmailService;
import io.payguard.userservice.domain.merchant.value.Email;
import io.payguard.userservice.integration.web.common.ErrorResponse;
import io.payguard.userservice.integration.web.common.ValidationErrorResponse;
import io.payguard.userservice.integration.web.identity.request.RegisterMerchantIdentityRequest;
import io.payguard.userservice.integration.web.identity.request.ResendMerchantVerificationEmailRequest;
import io.payguard.userservice.integration.web.identity.response.RegisterMerchantIdentityResponse;
import io.payguard.userservice.integration.web.identity.response.ResendMerchantVerificationEmailResponse;
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
@RequestMapping("/api/v1/merchant-registrations")
@RequiredArgsConstructor
@Tag(
        name = "Merchant registration",
        description = "Public merchant identity registration."
)
public class MerchantIdentityRegistrationController {

    private final RegisterMerchantIdentityService registrationService;
    private final ResendMerchantVerificationEmailService resendVerificationEmailService;

    @Operation(
            summary = "Register merchant identity",
            description = """
                    Creates a Keycloak identity with a permanent password,
                    assigns the MERCHANT role and starts email verification
                    and OTP configuration.

                    This operation does not create a Merchant profile or a
                    Stripe Connected Account and does not issue authentication
                    tokens.
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
                                            RegisterMerchantIdentityResponse.class
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
    public RegisterMerchantIdentityResponse register(
            @Valid @RequestBody RegisterMerchantIdentityRequest request
    ) {

        registrationService.execute(
                new RegisterMerchantIdentityCommand(
                        Email.of(request.email()),
                        request.password()
                )
        );

        return RegisterMerchantIdentityResponse.pendingVerification();
    }

    @Operation(
            summary = "Resend merchant verification email",
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
                                            ResendMerchantVerificationEmailResponse.class
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
    public ResendMerchantVerificationEmailResponse resendVerificationEmail(
            @Valid @RequestBody
            ResendMerchantVerificationEmailRequest request
    ) {

        resendVerificationEmailService.execute(
                new ResendMerchantVerificationEmailCommand(
                        Email.of(request.email())
                )
        );

        return ResendMerchantVerificationEmailResponse.requested();
    }
}
