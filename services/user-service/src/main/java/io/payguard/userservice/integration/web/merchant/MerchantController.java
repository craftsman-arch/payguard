package io.payguard.userservice.integration.web.merchant;

import io.payguard.userservice.application.merchant.payment.MerchantOnboardingLinkService;
import io.payguard.userservice.application.merchant.query.CurrentMerchantQuery;
import io.payguard.userservice.application.merchant.query.CurrentMerchantQueryService;
import io.payguard.userservice.application.merchant.register.RegisterMerchantService;
import io.payguard.userservice.integration.web.common.ErrorResponse;
import io.payguard.userservice.integration.web.common.ValidationErrorResponse;
import io.payguard.userservice.integration.web.merchant.request.RegisterMerchantRequest;
import io.payguard.userservice.integration.web.merchant.response.CurrentMerchantResponse;
import io.payguard.userservice.integration.web.merchant.response.MerchantOnboardingLinkResponse;
import io.payguard.userservice.integration.web.merchant.response.RegisterMerchantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private final RegisterMerchantService registerMerchantService;
    private final CurrentMerchantQueryService currentMerchantQueryService;
    private final MerchantOnboardingLinkService merchantOnboardingLinkService;
    private final MerchantWebMapper mapper;

    @Operation(
            summary = "Register merchant",
            description = """
                Registers a new merchant and starts the onboarding process.

                The service creates a Keycloak user, provisions a Stripe
                Connected Account and returns a Stripe Connect onboarding URL.

                The client application should redirect the merchant to the
                returned onboarding URL to complete Stripe Connect onboarding.

                The merchant remains in the PENDING state until Stripe
                completes onboarding and sends an account.updated webhook,
                after which the merchant becomes ACTIVE.

                Registration is idempotent. If a merchant with the same
                email already exists, the existing merchant and a new
                onboarding URL are returned.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Merchant registered successfully. A Stripe Connect onboarding URL was generated.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = RegisterMerchantResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ValidationErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error during merchant onboarding.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterMerchantResponse register(
            @Valid @RequestBody RegisterMerchantRequest request
    ) {

        return mapper.toResponse(
                registerMerchantService.execute(
                        mapper.toCommand(request)
                )
        );
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
    @PreAuthorize("hasRole('MERCHANT')")
    @ResponseStatus(HttpStatus.OK)
    public CurrentMerchantResponse currentMerchant() {

        return mapper.toResponse(
                currentMerchantQueryService.execute(
                        new CurrentMerchantQuery()
                )
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
                    description = "Authenticated user does not have the MERCHANT role.",
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
    @PreAuthorize("hasRole('MERCHANT')")
    @ResponseStatus(HttpStatus.OK)
    public MerchantOnboardingLinkResponse createOnboardingLink() {

        return new MerchantOnboardingLinkResponse(
                merchantOnboardingLinkService
                        .execute()
                        .onboardingUrl()
        );
    }
}
