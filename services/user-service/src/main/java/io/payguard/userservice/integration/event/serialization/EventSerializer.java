package io.payguard.userservice.integration.event.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSerializer {

    private final ObjectMapper objectMapper;

    public String serialize(Object event) {

        try {
            return objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException exception) {

            throw new EventSerializationException(
                    "Unable to serialize integration event.",
                    exception
            );
        }
    }
}
