package io.payguard.apigateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private final RealmRoleAuthoritiesConverter authoritiesConverter;
    private final GatewayAuthenticationEntryPoint authenticationEntryPoint;
    private final GatewayAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/auth/**",
                                "/actuator/**",
                                "/api/merchants"
                        )
                        .permitAll()

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/webhooks/stripe"
                        )
                        .permitAll()

                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(this::convert)
                        )
                )
                .build();
    }

    private Mono<AbstractAuthenticationToken> convert(
            org.springframework.security.oauth2.jwt.Jwt jwt
    ) {

        return Mono.just(
                new JwtAuthenticationToken(
                        jwt,
                        authoritiesConverter.convert(jwt)
                )
        );
    }

}
