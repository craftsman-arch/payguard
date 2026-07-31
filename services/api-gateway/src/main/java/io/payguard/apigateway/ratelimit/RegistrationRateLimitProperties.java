package io.payguard.apigateway.ratelimit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gateway.rate-limit.registration")
public record RegistrationRateLimitProperties(

        @NotNull
        @Valid
        Limit identity,

        @NotNull
        @Valid
        Limit verificationEmail

) {

    public record Limit(

            @Min(1)
            int replenishRate,

            @Min(1)
            int burstCapacity,

            @Min(1)
            int requestedTokens

    ) {
    }
}
