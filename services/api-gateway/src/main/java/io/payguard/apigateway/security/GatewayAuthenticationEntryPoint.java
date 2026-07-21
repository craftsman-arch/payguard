package io.payguard.apigateway.security;

import io.payguard.apigateway.exception.ErrorResponseFactory;
import io.payguard.apigateway.exception.ErrorResponseWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GatewayAuthenticationEntryPoint
        implements ServerAuthenticationEntryPoint {

    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;

    private final ErrorResponseFactory errorResponseFactory;
    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public Mono<Void> commence(@NonNull ServerWebExchange exchange, @NonNull AuthenticationException exception) {

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