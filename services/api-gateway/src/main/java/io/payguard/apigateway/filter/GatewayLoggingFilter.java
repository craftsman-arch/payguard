package io.payguard.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long started = System.nanoTime();

        ServerHttpRequest request = exchange.getRequest();

        String correlationId =
                exchange.getAttribute(CorrelationId.ATTRIBUTE);

        log.info(
                "Incoming request [{} {}] [correlationId={}]",
                request.getMethod(),
                request.getURI().getPath(),
                correlationId
        );

        return chain.filter(exchange)
                .doFinally(signal -> {

                    long duration =
                            Duration.ofNanos(
                                    System.nanoTime() - started
                            ).toMillis();

                    HttpStatusCode status =
                            exchange.getResponse().getStatusCode();

                    log.info(
                            "Completed request [{} {}] [status={}] [duration={} ms] [correlationId={}]",
                            request.getMethod(),
                            request.getURI().getPath(),
                            status != null ? status.value() : 0,
                            duration,
                            correlationId
                    );
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}