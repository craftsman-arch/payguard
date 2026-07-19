package io.payguard.userservice.integration.payment.events;

import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.integration.payment.config.StripeProperties;
import io.payguard.userservice.integration.payment.error.exception.StripeSignatureVerificationException;
import io.payguard.userservice.integration.payment.events.dto.StripeSignature;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class StripeSignatureVerifier {

    private static final long TOLERANCE_SECONDS = 300;
    private static final String SIGNED_PAYLOAD_SEPARATOR = ".";

    private final StripeProperties properties;
    private final StripeSignatureParser parser;
    private final TimeProvider timeProvider;

    public void verify(String payload, String signatureHeader) {

        StripeSignature signature = parser.parse(signatureHeader);
        verifyTimestamp(signature.timestamp());
        String expectedSignature = calculateSignature(signature.timestamp(), payload);
        verifySignature(expectedSignature, signature.signature());

    }

    private String calculateSignature(long timestamp, String payload) {

        String signedPayload = timestamp + SIGNED_PAYLOAD_SEPARATOR + payload;

        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, properties.webhookSecret().getBytes(StandardCharsets.UTF_8))
                .hmacHex(signedPayload);
    }

    private void verifySignature(String expected, String actual) {

        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        )) {

            throw new StripeSignatureVerificationException("Stripe signature verification failed.");
        }
    }

    private void verifyTimestamp(long timestamp) {

        if (Math.abs(currentEpochSecond() - timestamp) > TOLERANCE_SECONDS) {

            throw new StripeSignatureVerificationException("Stripe signature has expired.");
        }
    }

    private long currentEpochSecond() {
        return timeProvider.now().getEpochSecond();
    }

}