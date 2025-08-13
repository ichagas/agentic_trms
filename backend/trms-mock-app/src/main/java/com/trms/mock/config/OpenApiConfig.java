package com.trms.mock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("Local development server"),
                        new Server().url("https://api.trms-mock.com").description("Production server")
                ))
                .info(new Info()
                        .title("TRMS Mock API")
                        .description("Mock Treasury and Risk Management System API for AI Agent POC\n\n" +
                                    "This API provides comprehensive financial operations including:\n" +
                                    "- Account management and balance inquiries\n" +
                                    "- Transaction processing and booking\n" +
                                    "- End-of-day processing and readiness checks\n" +
                                    "- Market data status monitoring\n" +
                                    "- Rate fixing proposals and management\n" +
                                    "- Financial reporting\n\n" +
                                    "The system simulates a production-grade treasury system with realistic " +
                                    "financial data and complex business logic for EOD processing.")
                        .version("1.0.0")
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                        .contact(new Contact()
                                .name("TRMS Development Team")
                                .email("dev@trms-mock.com")
                                .url("https://github.com/trms-ai-poc")))
                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                        .description("TRMS AI Agent Documentation")
                        .url("https://docs.trms-ai-poc.com"));
    }
}