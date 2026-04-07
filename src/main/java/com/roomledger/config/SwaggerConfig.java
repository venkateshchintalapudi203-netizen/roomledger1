package com.roomledger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RoomLedger API")
                        .description("Room Expense Tracker — track shared expenses, budgets, and members across multiple rooms.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RoomLedger")
                                .email("support@roomledger.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Dev Server")
                ))
                // Add JWT Bearer auth button in Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here. Get it from /api/auth/login or /api/auth/register")));
    }
}