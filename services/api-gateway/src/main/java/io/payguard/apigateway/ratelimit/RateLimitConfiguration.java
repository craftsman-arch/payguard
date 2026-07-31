package io.payguard.apigateway.ratelimit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RateLimitConfiguration {

    @Bean
    @Primary
    @Qualifier("merchantIdentityRegistrationRateLimiter")
    public RedisRateLimiter merchantIdentityRegistrationRateLimiter(RegistrationRateLimitProperties properties) {

        return create(properties.identity());
    }

    @Bean
    @Qualifier("merchantVerificationEmailRateLimiter")
    public RedisRateLimiter merchantVerificationEmailRateLimiter(RegistrationRateLimitProperties properties) {

        return create(properties.verificationEmail());
    }

    private RedisRateLimiter create(RegistrationRateLimitProperties.Limit limit) {

        return new RedisRateLimiter(
                limit.replenishRate(),
                limit.burstCapacity(),
                limit.requestedTokens()
        );
    }
}
