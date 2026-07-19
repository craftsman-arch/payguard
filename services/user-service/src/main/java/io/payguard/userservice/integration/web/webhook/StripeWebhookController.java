package io.payguard.userservice.integration.web.webhook;

import io.payguard.userservice.application.payment.events.ProcessStripeEventCommand;
import io.payguard.userservice.application.payment.events.ProcessStripeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhooks/stripe")
public class StripeWebhookController {

    private static final String STRIPE_SIGNATURE_HEADER =
            "Stripe-Signature";

    private final ProcessStripeEventService processStripeEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void handleEvent(

            @RequestBody
            String payload,

            @RequestHeader(STRIPE_SIGNATURE_HEADER)
            String signature
    ) {

        processStripeEventService.execute(
                new ProcessStripeEventCommand(
                        payload,
                        signature
                )
        );
    }

}