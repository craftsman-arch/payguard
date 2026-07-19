package io.payguard.userservice.integration.payment.events;

import io.payguard.userservice.integration.payment.error.exception.StripeSignatureVerificationException;
import io.payguard.userservice.integration.payment.events.dto.StripeSignature;
import org.springframework.stereotype.Component;

@Component
public class StripeSignatureParser {

    public StripeSignature parse(String header) {

        long timestamp = 0;
        String signature = null;

        for (String part : header.split(",")) {

            String[] pair = part.split("=", 2);

            if (pair.length != 2) {
                continue;
            }

            switch (pair[0]) {

                case "t" -> timestamp =
                        Long.parseLong(pair[1]);

                case "v1" -> signature =
                        pair[1];
            }
        }

        if (timestamp == 0 || signature == null) {
            throw new StripeSignatureVerificationException(
                    "Invalid Stripe-Signature header."
            );
        }

        return new StripeSignature(
                timestamp,
                signature
        );
    }

}