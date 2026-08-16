package com.telecom.campaign.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OpenApiConfig {

    @Bean
    public OpenAPI telecomCampaignOpenAPI() {
                log.info("Creating OpenAPI definition for Telecom Campaign Management API");
        return new OpenAPI().info(new Info()
                        .title("Telecom Campaign Management API")
                        .description("""
                                REST APIs for Telecom Campaign Management Platform.
                                Features:
                                - Authentication
                                - Campaign Management
                                - Customer Management
                                - Notifications
                                - Analytics
                                """)
                        .version("v1.0.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation")
                        .url("https://github.com/GouthamV10/telecom-campaign-management-platform")
                ).components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                ).addSecurityItem( new SecurityRequirement()
                        .addList("bearerAuth")
                );
    }
}
