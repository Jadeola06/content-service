package com.flexydemy.content.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String port;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:" + port + contextPath)
                .description("Local Server");

        Server gatewayServer = new Server()
                .url("http://localhost:4000/api")
                .description("API-Gateway-Local");

        return new OpenAPI()
                .info(new Info()
                        .title("Authorization Server")
                        .description("For Authorizing Users")
                        .version("v1.0.0")
                        .summary("Content Management etc..")
                        .termsOfService("Flexy Demy")
                        .contact(new Contact()
                                .name("Flexy Demy")
                                .email("info@flexydemy.com")
                                .url("https://www.flexy-demy.com"))
                        .license(new License().name("Flexy Demy"))
                )
                .addSecurityItem(new SecurityRequirement().addList("auth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("auth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("JWT Authorization header using the Bearer scheme")))
                .servers(List.of(localServer, gatewayServer));
    }
    private io.swagger.v3.oas.models.Components buildSecurityComponents() {
        return new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("""
                            **JWT Authentication**
                            - Obtain token from /api/v1/auth/login
                            - Prefix with 'Bearer ' in Authorization header
                            """))
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("Basic auth for documentation access"));
    }
}
