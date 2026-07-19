package io.payguard.apigateway.routing;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteConfiguration {

    private final GatewayRoutesProperties properties;

    @Bean
    RouteLocator routeLocator(
            RouteLocatorBuilder builder
    ) {

        return builder.routes()

                .route(
                        "user-service",
                        route -> route

                                .path("/api/v1/merchants/**")

                                .uri(
                                        properties.userService().uri()
                                )
                )

                .route(
                        "keycloak",
                        route -> route

                                .path("/auth/**")

                                .filters(filter ->

                                        filter.rewritePath(
                                                "/auth/(?<segment>.*)",
                                                "/realms/payguard/protocol/openid-connect/${segment}"
                                        )
                                )

                                .uri(
                                        properties.keycloak().uri()
                                )
                )

                .build();
    }

}