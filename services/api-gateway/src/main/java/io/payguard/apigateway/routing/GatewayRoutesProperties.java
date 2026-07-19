package io.payguard.apigateway.routing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.routes")
public record GatewayRoutesProperties(

        ServiceRoute userService,

        ServiceRoute keycloak

) {

    public record ServiceRoute(
            String uri
    ) {
    }

}