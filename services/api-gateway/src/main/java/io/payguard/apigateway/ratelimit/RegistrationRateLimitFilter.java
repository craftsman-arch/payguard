package io.payguard.apigateway.ratelimit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RegistrationRateLimitFilter {

    private static final String IDENTITY_REGISTRATION = "merchant-identity-registration";
    private static final String VERIFICATION_EMAIL = "merchant-verification-email";
    private static final String RETRY_AFTER_SECONDS = "1";
    private static final String UNAVAILABLE_REMAINING = "-1";

    private final KeyResolver keyResolver;
    private final RedisRateLimiter identityRateLimiter;
    private final RedisRateLimiter verificationEmailRateLimiter;
    private final MeterRegistry meterRegistry;

    public RegistrationRateLimitFilter(
            ClientIpKeyResolver keyResolver,
            @Qualifier("merchantIdentityRegistrationRateLimiter")
            RedisRateLimiter identityRateLimiter,
            @Qualifier("merchantVerificationEmailRateLimiter")
            RedisRateLimiter verificationEmailRateLimiter,
            MeterRegistry meterRegistry
    ) {

        this.keyResolver = keyResolver;
        this.identityRateLimiter = identityRateLimiter;
        this.verificationEmailRateLimiter = verificationEmailRateLimiter;
        this.meterRegistry = meterRegistry;
    }

    public GatewayFilter identityRegistration() {

        return filter(IDENTITY_REGISTRATION, identityRateLimiter);
    }

    public GatewayFilter verificationEmail() {

        return filter(VERIFICATION_EMAIL, verificationEmailRateLimiter);
    }

    private GatewayFilter filter(String operation, RedisRateLimiter rateLimiter) {

        Counter allowed = counter(operation, "allowed");
        Counter rejected = counter(operation, "rejected");
        Counter unavailable = counter(operation, "unavailable");

        return (exchange, chain) -> keyResolver.resolve(exchange)
                .flatMap(key -> rateLimiter
                        .isAllowed(operation, key)
                        .onErrorMap(exception -> {

                            unavailable.increment();
                            log.error(
                                    "Registration rate limiter [{}] is unavailable.",
                                    operation,
                                    exception
                            );

                            return new RateLimitUnavailableException(
                                    exception
                            );
                        })
                )
                .flatMap(response -> {

                    if (isUnavailable(
                            rateLimiter,
                            response.getHeaders()
                    )) {

                        unavailable.increment();
                        log.error(
                                "Registration rate limiter [{}] is unavailable.",
                                operation
                        );

                        return reactor.core.publisher.Mono.error(
                                new RateLimitUnavailableException()
                        );
                    }

                    copyHeaders(
                            exchange.getResponse().getHeaders(),
                            response.getHeaders()
                    );

                    if (!response.isAllowed()) {

                        rejected.increment();
                        exchange.getResponse().getHeaders().set(
                                HttpHeaders.RETRY_AFTER,
                                RETRY_AFTER_SECONDS
                        );

                        return reactor.core.publisher.Mono.error(
                                new RateLimitExceededException()
                        );
                    }

                    allowed.increment();
                    return chain.filter(exchange);
                });
    }

    private boolean isUnavailable(RedisRateLimiter rateLimiter, Map<String, String> headers) {

        return UNAVAILABLE_REMAINING.equals(
                headers.get(
                        rateLimiter.getRemainingHeader()
                )
        );
    }

    private void copyHeaders(HttpHeaders target, Map<String, String> source) {

        source.forEach(target::set);
    }

    private Counter counter(String operation, String outcome) {

        return Counter.builder(
                        "gateway.registration.rate.limit.requests"
                )
                .description(
                        "Registration requests evaluated by the distributed rate limiter."
                )
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry);
    }
}
