package io.payguard.apigateway.exception;

import io.payguard.apigateway.filter.CorrelationId;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Component
public class ErrorResponseFactory {

    private static final String INTERNAL_ERROR_MESSAGE =
            "An unexpected error occurred.";

    public ErrorResponse create(
            @NonNull ServerWebExchange exchange,
            @NonNull HttpStatus status,
            @NonNull Throwable exception
    ) {

        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                resolveMessage(status, exception),
                exchange.getRequest().getPath().value(),
                exchange.getAttribute(CorrelationId.ATTRIBUTE)
        );
    }

    private String resolveMessage(
            HttpStatus status,
            Throwable exception
    ) {

        if (status.is5xxServerError()) {
            return INTERNAL_ERROR_MESSAGE;
        }

        if (exception instanceof ResponseStatusException responseException
                && responseException.getReason() != null) {

            return responseException.getReason();
        }

        String message = exception.getMessage();

        return message != null
                ? message
                : status.getReasonPhrase();
    }

}
