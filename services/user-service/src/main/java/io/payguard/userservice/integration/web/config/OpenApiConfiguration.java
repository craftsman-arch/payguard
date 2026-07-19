package io.payguard.userservice.integration.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI userServiceOpenApi() {

        return new OpenAPI()

                .info(
                        new Info()
                                .title("PayGuard User Service API")
                                .version("v1")
                                .description("""
                                        User Service is responsible for merchant registration,
                                        onboarding, authentication, authorization,
                                        and merchant profile management.
                                        """)
                                .contact(
                                        new Contact()
                                                .name("Sergei Sukhoborov")
                                                .email("sergei.sukhoborov@gmail.com")
                                )
                                .license(
                                        new License()
                                                .name("MIT")
                                )
                )

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )

                .externalDocs(
                        new ExternalDocumentation()
                                .description("PayGuard GitHub Repository")
                                .url("https://github.com/craftsman-arch/payguard")
                );
    }

}