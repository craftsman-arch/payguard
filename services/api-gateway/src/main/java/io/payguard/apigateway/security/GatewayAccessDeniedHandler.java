package io.payguard.apigateway.security;

import io.payguard.apigateway.exception.ErrorResponseFactory;
import io.payguard.apigateway.exception.ErrorResponseWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GatewayAccessDeniedHandler
        implements ServerAccessDeniedHandler {

    private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;

    private final ErrorResponseFactory errorResponseFactory;
    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull AccessDeniedException exception) {

        exchange.getResponse().setStatusCode(STATUS);

        return errorResponseWriter.write(
                exchange.getResponse(),
                errorResponseFactory.create(
                        exchange,
                        STATUS,
                        exception
                )
        );
    }

}