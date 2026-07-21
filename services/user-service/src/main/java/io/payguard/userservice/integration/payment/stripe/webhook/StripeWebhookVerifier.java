package io.payguard.userservice.integration.payment.stripe.webhook;

import io.payguard.userservice.application.payment.webhook.PaymentProviderWebhookVerifier;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.integration.payment.config.StripeProperties;
import io.payguard.userservice.integration.payment.error.exception.StripeSignatureVerificationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StripeWebhookVerifier
        implements PaymentProviderWebhookVerifier {

    private static final long TOLERANCE_SECONDS = 300;
    private static final String SIGNED_PAYLOAD_SEPARATOR = ".";

    private final StripeProperties properties;
    private final TimeProvider timeProvider;

    @Override
    public void verify(String payload, String signatureHeader) {

        ParsedStripeSignature signature = parseSignatureHeader(signatureHeader);
        verifyTimestamp(signature.timestamp());
        String expectedSignature = calculateSignature(signature.timestamp(), payload);
        verifyAnySignature(expectedSignature, signature.signatures());
    }

    private ParsedStripeSignature parseSignatureHeader(String signatureHeader) {

        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw invalidSignatureHeader();
        }

        Long timestamp = null;
        List<String> signatures = new ArrayList<>();

        for (String part : signatureHeader.split(",")) {

            String[] pair = part.trim().split("=", 2);

            if (pair.length != 2) {
                continue;
            }

            String key = pair[0].trim();
            String value = pair[1].trim();

            if (value.isBlank()) {
                continue;
            }

            switch (key) {

                case "t" -> timestamp = parseTimestamp(
                        value
                );

                case "v1" -> signatures.add(
                        value
                );

                default -> {

                }
            }
        }

        if (timestamp == null || signatures.isEmpty()) {
            throw invalidSignatureHeader();
        }

        return new ParsedStripeSignature(timestamp, List.copyOf(signatures));
    }

    private long parseTimestamp(String value) {

        try {
            return Long.parseLong(
                    value
            );

        } catch (NumberFormatException ex) {
            throw invalidSignatureHeader();
        }
    }

    private String calculateSignature(long timestamp, String payload) {

        String signedPayload =
                timestamp
                        + SIGNED_PAYLOAD_SEPARATOR
                        + payload;

        return new HmacUtils(
                HmacAlgorithms.HMAC_SHA_256,
                properties.webhookSecret()
                        .getBytes(StandardCharsets.UTF_8)
        ).hmacHex(
                signedPayload
        );
    }

    private void verifyAnySignature(String expectedSignature, List<String> actualSignatures) {

        byte[] expectedBytes = expectedSignature.getBytes(
                StandardCharsets.UTF_8
        );

        boolean valid = actualSignatures.stream()
                .anyMatch(actualSignature ->
                        MessageDigest.isEqual(
                                expectedBytes,
                                actualSignature.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        )
                );

        if (!valid) {
            throw new StripeSignatureVerificationException(
                    "Stripe signature verification failed."
            );
        }
    }

    private void verifyTimestamp(long timestamp) {

        long currentEpochSecond = timeProvider.now()
                .getEpochSecond();

        if (Math.abs(currentEpochSecond - timestamp) > TOLERANCE_SECONDS) {
            throw new StripeSignatureVerificationException(
                    "Stripe signature has expired."
            );
        }
    }

    private StripeSignatureVerificationException invalidSignatureHeader() {
        return new StripeSignatureVerificationException(
                "Invalid Stripe-Signature header."
        );
    }

    private record ParsedStripeSignature(
            long timestamp,
            List<String> signatures
    ) {
    }
}