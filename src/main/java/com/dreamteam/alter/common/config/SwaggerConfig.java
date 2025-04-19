package com.dreamteam.alter.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenApi(ServletContext servletContext) {
        Server servers = new Server().url(servletContext.getContextPath());

        SecurityScheme jwtAuthScheme = new SecurityScheme()
            .name("Authorization")
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
            .info(new Info()
                .title("ALTER API")
                .version("v1.0.0"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", jwtAuthScheme))
            .servers(List.of(servers))
            .addSecurityItem(securityRequirement);
    }

}
