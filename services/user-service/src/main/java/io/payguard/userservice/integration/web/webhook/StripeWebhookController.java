package io.payguard.userservice.integration.web.webhook;

import io.payguard.userservice.application.payment.webhook.ProcessPaymentProviderWebhookCommand;
import io.payguard.userservice.application.payment.webhook.ProcessPaymentProviderWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhooks/stripe")
public class StripeWebhookController {

    private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

    private final ProcessPaymentProviderWebhookService processWebhookService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void handleEvent(

            @RequestBody
            String payload,

            @RequestHeader(STRIPE_SIGNATURE_HEADER)
            String signature
    ) {

        processWebhookService.execute(
                new ProcessPaymentProviderWebhookCommand(
                        payload,
                        signature
                )
        );
    }
}