package io.payguard.apigateway;

import io.payguard.apigateway.routing.GatewayRoutesProperties;
import io.payguard.apigateway.ratelimit.RegistrationRateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Hooks;

@EnableConfigurationProperties(
        {
                GatewayRoutesProperties.class,
                RegistrationRateLimitProperties.class
        }
)
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {

        Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
