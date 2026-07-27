package io.payguard.userservice.integration.event.outbox;

import org.springframework.stereotype.Component;

@Component
public class OutboxPublicationErrorSanitizer {

    private static final int MAX_ERROR_LENGTH = 1000;

    public String sanitize(Throwable exception) {

        Throwable cause = rootCause(exception);

        String message = cause.getMessage();

        String sanitizedMessage =
                message == null || message.isBlank()
                        ? cause.getClass().getSimpleName()
                        : cause.getClass().getSimpleName()
                                + ": "
                                + message;

        sanitizedMessage = sanitizedMessage
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();

        return sanitizedMessage.length() <= MAX_ERROR_LENGTH
                ? sanitizedMessage
                : sanitizedMessage.substring(
                        0,
                        MAX_ERROR_LENGTH
                );
    }

    private Throwable rootCause(Throwable exception) {

        Throwable result = exception;

        while (result.getCause() != null
                && result.getCause() != result) {

            result = result.getCause();
        }

        return result;
    }
}
