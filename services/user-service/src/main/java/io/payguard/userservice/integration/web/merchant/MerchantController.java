package io.payguard.userservice.integration.web.merchant;

import io.payguard.userservice.application.merchant.payment.MerchantOnboardingLinkService;
import io.payguard.userservice.application.merchant.payment.StartMerchantPaymentOnboardingService;
import io.payguard.userservice.application.merchant.profile.CreateMerchantProfileCommand;
import io.payguard.userservice.application.merchant.profile.CreateMerchantProfileResult;
import io.payguard.userservice.application.merchant.profile.CreateMerchantProfileService;
import io.payguard.userservice.application.merchant.query.CurrentMerchantQuery;
import io.payguard.userservice.application.merchant.query.CurrentMerchantQueryService;
import io.payguard.userservice.domain.merchant.value.Country;
import io.payguard.userservice.integration.web.common.ErrorResponse;
import io.payguard.userservice.integration.web.common.ValidationErrorResponse;
import io.payguard.userservice.integration.web.merchant.request.CreateMerchantProfileRequest;
import io.payguard.userservice.integration.web.merchant.response.CreateMerchantProfileResponse;
import io.payguard.userservice.integration.web.merchant.response.CurrentMerchantResponse;
import io.payguard.userservice.integration.web.merchant.response.MerchantOnboardingLinkResponse;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Tag(
        name = "Merchants",
        description = "Merchant registration and profile management."
)
public class MerchantController {

    private final CreateMerchantProfileService createMerchantProfileService;
    private final CurrentMerchantQueryService currentMerchantQueryService;
    private final MerchantOnboardingLinkService merchantOnboardingLinkService;
    private final StartMerchantPaymentOnboardingService startPaymentOnboardingService;
    private final MerchantWebMapper mapper;

    @Operation(
            summary = "Create merchant profile",
            description = """
                Creates the business profile for the authenticated merchant.
                Identity ownership and email are derived from the verified JWT;
                the browser supplies only business information.

                Repeating the request for the same authenticated identity
                returns the existing Merchant instead of creating a duplicate.
                Stripe onboarding is started through a separate endpoint.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Merchant profile created.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CreateMerchantProfileResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Existing Merchant profile returned.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CreateMerchantProfileResponse.class
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
                    responseCode = "401",
                    description = "Authentication is missing or invalid."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "USER role or verified email is missing.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreateMerchantProfileResponse> createProfile(
            @Valid @RequestBody CreateMerchantProfileRequest request
    ) {

        CreateMerchantProfileResult result =
                createMerchantProfileService.execute(
                        new CreateMerchantProfileCommand(
                                request.legalName(),
                                request.businessType(),
                                Country.of(request.country())
                        )
                );

        CreateMerchantProfileResponse response =
                new CreateMerchantProfileResponse(
                        result.id(),
                        result.status()
                );

        return ResponseEntity
                .status(
                        result.created()
                                ? HttpStatus.CREATED
                                : HttpStatus.OK
                )
                .body(response);
    }

    @Operation(
            summary = "Get current merchant",
            description = "Returns the authenticated merchant profile."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant profile returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CurrentMerchantResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Merchant not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public CurrentMerchantResponse currentMerchant() {

        return mapper.toResponse(
                currentMerchantQueryService.execute(
                        new CurrentMerchantQuery()
                )
        );
    }

    @Operation(
            summary = "Start merchant payment onboarding",
            description = """
                    Creates a Stripe Connected Account for the authenticated
                    Merchant when one does not exist, persists the account
                    linkage and returns a short-lived Stripe Account Link.

                    Repeated calls reuse the existing Connected Account and
                    create only a fresh Account Link.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stripe onboarding link created.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation =
                                            MerchantOnboardingLinkResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or invalid."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated identity does not have the USER role.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Merchant profile was not found.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A previous Stripe account-creation result requires reconciliation.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Payment provider rejected the request.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Payment provider is temporarily unavailable.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/me/payment-account/onboarding")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public MerchantOnboardingLinkResponse startPaymentOnboarding() {

        return new MerchantOnboardingLinkResponse(
                startPaymentOnboardingService
                        .execute()
                        .onboardingUrl()
        );
    }

    @Operation(
            summary = "Create payment-account onboarding link",
            description = """
                    Creates a new Stripe Account Link for the authenticated
                    merchant's existing Connected Account. This endpoint never
                    creates a second Connected Account.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stripe onboarding link created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = MerchantOnboardingLinkResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Merchant is not eligible for payment-account re-onboarding.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or invalid."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated identity does not have the USER role.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Merchant not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Payment provider rejected the onboarding-link request.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Payment provider is temporarily unavailable.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/me/payment-account/onboarding-link")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public MerchantOnboardingLinkResponse createOnboardingLink() {

        return new MerchantOnboardingLinkResponse(
                merchantOnboardingLinkService
                        .execute()
                        .onboardingUrl()
        );
    }
}
