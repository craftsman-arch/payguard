package io.payguard.apigateway.ratelimit;

import lombok.NonNull;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
public class ClientIpKeyResolver implements KeyResolver {

    private static final String UNKNOWN_CLIENT = "unknown";

    @Override
    public Mono<String> resolve(@NonNull ServerWebExchange exchange) {

        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();

        String clientIp = remoteAddress == null
                || remoteAddress.getAddress() == null
                ? UNKNOWN_CLIENT
                : remoteAddress.getAddress().getHostAddress();

        return Mono.just(clientIp);
    }
}
