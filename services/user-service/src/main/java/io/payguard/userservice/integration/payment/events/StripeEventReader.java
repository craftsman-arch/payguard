package io.payguard.userservice.integration.payment.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.payguard.userservice.integration.payment.events.dto.StripeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeEventReader {

    private final ObjectMapper objectMapper;

    public StripeEvent read(String payload) {

        try {

            return objectMapper.readValue(payload, StripeEvent.class);

        } catch (Exception ex) {

            throw new IllegalArgumentException(
                    "Unable to deserialize Stripe event.",
                    ex
            );
        }
    }

    public <T> T read(JsonNode node, Class<T> type) {

        try {

            return objectMapper.treeToValue(node, type);

        } catch (Exception ex) {

            throw new IllegalArgumentException(
                    "Unable to deserialize Stripe event payload.",
                    ex
            );
        }
    }

}