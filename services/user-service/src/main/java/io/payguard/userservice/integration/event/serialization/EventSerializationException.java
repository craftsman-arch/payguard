package io.payguard.userservice.integration.event.serialization;

public class EventSerializationException extends RuntimeException {

    public EventSerializationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
