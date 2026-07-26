package com.telecom.campaign.config.OpenApiConfig;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI telecomCampaignOpenAPI() {
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
                );
    }
}
