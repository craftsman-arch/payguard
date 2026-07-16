package io.payguard.apigateway.exception;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler
        implements ErrorWebExceptionHandler {

    private final GatewayExceptionMapper mapper;
    private final ErrorResponseFactory factory;
    private final ErrorResponseWriter writer;

    @Override
    public Mono<Void> handle(
            @NonNull ServerWebExchange exchange,
            @NonNull Throwable exception
    ) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }

        HttpStatus status = mapper.map(exception);

        if (status.is5xxServerError()) {
            log.error("Gateway request failed.", exception);
        }

        exchange.getResponse().setStatusCode(status);

        return writer.write(
                exchange.getResponse(),
                factory.create(
                        exchange,
                        status,
                        exception
                )
        );
    }

}