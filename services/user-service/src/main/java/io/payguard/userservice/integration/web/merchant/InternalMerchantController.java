package io.payguard.userservice.integration.web.merchant;

import io.payguard.userservice.application.merchant.payment.MerchantPaymentContext;
import io.payguard.userservice.application.merchant.payment.MerchantPaymentContextQueryService;
import io.payguard.userservice.integration.web.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/merchants")
@RequiredArgsConstructor
@Tag(
        name = "Internal Merchants",
        description = "Trusted service-to-service merchant contracts."
)
public class InternalMerchantController {

    private final MerchantPaymentContextQueryService merchantPaymentContextQueryService;

    @Operation(
            summary = "Get merchant payment context",
            description = """
                    Returns trusted Stripe account and payment eligibility data.
                    This internal endpoint requires the
                    merchant.payment-context.read OAuth scope and is not routed
                    through API Gateway.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Merchant payment context returned successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = MerchantPaymentContext.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Service authentication is missing or invalid."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Required merchant payment-context scope is absent.",
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
            )
    })
    @GetMapping("/{merchantId}/payment-context")
    @PreAuthorize("hasAuthority('SCOPE_merchant.payment-context.read')")
    public MerchantPaymentContext paymentContext(@PathVariable UUID merchantId) {

        return merchantPaymentContextQueryService.execute(merchantId);
    }
}
