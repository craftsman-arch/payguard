package io.payguard.apigateway;

import io.payguard.apigateway.routing.GatewayRoutesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Hooks;

@EnableConfigurationProperties(
        GatewayRoutesProperties.class
)
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {

        Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
