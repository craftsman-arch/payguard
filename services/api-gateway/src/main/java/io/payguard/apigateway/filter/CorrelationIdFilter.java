package io.payguard.apigateway.filter;

import lombok.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            @NonNull ServerWebExchange exchange,
            @NonNull GatewayFilterChain chain
    ) {

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CorrelationId.HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        exchange.getAttributes().put(
                CorrelationId.ATTRIBUTE,
                correlationId
        );

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(CorrelationId.HEADER, correlationId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request)
                .build();

        mutatedExchange.getResponse()
                .getHeaders()
                .set(CorrelationId.HEADER, correlationId);

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}