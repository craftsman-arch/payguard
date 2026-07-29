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
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                .route(
                        "merchant-identity-registration",
                        route -> route
                                .path("/api/merchant-registrations")
                                .filters(filter ->
                                        filter.setPath(
                                                "/api/v1/merchant-registrations"
                                        )
                                )
                                .uri(
                                        properties.userService().uri()
                                )
                )
                .route(
                        "merchant-verification-email",
                        route -> route
                                .path(
                                        "/api/merchant-registrations/verification-email"
                                )
                                .filters(filter ->
                                        filter.setPath(
                                                "/api/v1/merchant-registrations/verification-email"
                                        )
                                )
                                .uri(
                                        properties.userService().uri()
                                )
                )
                .route(
                        "merchant-profile",
                        route -> route
                                .path("/api/merchants")
                                .filters(filter ->
                                        filter.setPath(
                                                "/api/v1/merchants"
                                        )
                                )
                                .uri(
                                        properties.userService().uri()
                                )
                )
                .route(
                        "user-service",
                        route -> route
                                .path("/api/merchants/**")
                                .filters(filter ->
                                        filter.rewritePath(
                                                "/api/merchants/(?<segment>.*)",
                                                "/api/v1/merchants/${segment}"
                                        )
                                )
                                .uri(properties.userService().uri())
                )
                .route(
                        "stripe-webhooks",
                        route -> route
                                .path("/api/webhooks/stripe")
                                .filters(filter ->
                                        filter.rewritePath(
                                                "/api/webhooks/stripe",
                                                "/api/v1/webhooks/stripe"
                                        )
                                )
                                .uri(properties.userService().uri())
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
