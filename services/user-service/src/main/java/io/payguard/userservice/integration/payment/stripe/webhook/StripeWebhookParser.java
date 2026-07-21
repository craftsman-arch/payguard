package io.payguard.userservice.integration.payment.stripe.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.payguard.userservice.application.payment.webhook.*;
import io.payguard.userservice.integration.payment.error.exception.StripeEventDeserializationException;
import io.payguard.userservice.integration.payment.stripe.webhook.dto.StripeAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeWebhookParser implements PaymentProviderWebhookParser {

    private static final String ACCOUNT_UPDATED_EVENT = "account.updated";

    private final ObjectMapper objectMapper;

    @Override
    public PaymentProviderWebhook parse(String payload) {

        try {

            JsonNode event = objectMapper.readTree(payload);

            String eventId = requiredText(event, "id");
            String eventType = requiredText(event, "type");

            return switch (eventType) {

                case ACCOUNT_UPDATED_EVENT -> parseAccountUpdated(
                        eventId,
                        event
                );

                default -> new PaymentProviderWebhook(
                        eventId,
                        PaymentProviderEventType.UNKNOWN,
                        new UnknownPaymentProviderWebhookPayload()
                );
            };

        } catch (StripeEventDeserializationException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new StripeEventDeserializationException(
                    "Unable to deserialize Stripe webhook event.",
                    ex
            );
        }
    }

    private PaymentProviderWebhook parseAccountUpdated(String eventId, JsonNode event) {

        JsonNode accountNode = event.path("data").path("object");

        if (accountNode.isMissingNode() || accountNode.isNull()) {
            throw deserializationError(
                    "Stripe account.updated event does not contain data.object."
            );
        }

        try {

            StripeAccountDto account = objectMapper.treeToValue(accountNode, StripeAccountDto.class);

            if (account.id() == null || account.id().isBlank()) {
                throw deserializationError(
                        "Stripe account.updated event does not contain an account id."
                );
            }

            return new PaymentProviderWebhook(
                    eventId,
                    PaymentProviderEventType.ACCOUNT_UPDATED,
                    new PaymentAccountUpdated(
                            account.id(),
                            account.chargesEnabled(),
                            account.payoutsEnabled()
                    )
            );

        } catch (StripeEventDeserializationException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new StripeEventDeserializationException(
                    "Unable to deserialize Stripe account.updated webhook payload.",
                    ex
            );
        }
    }

    private String requiredText(JsonNode node, String fieldName) {

        String value = node.path(fieldName).asText();

        if (value == null || value.isBlank()) {
            throw deserializationError(
                    "Stripe webhook event does not contain [%s]."
                            .formatted(fieldName)
            );
        }

        return value;
    }

    private StripeEventDeserializationException deserializationError(String message) {

        return new StripeEventDeserializationException(
                message,
                new IllegalArgumentException(
                        message
                )
        );
    }
}