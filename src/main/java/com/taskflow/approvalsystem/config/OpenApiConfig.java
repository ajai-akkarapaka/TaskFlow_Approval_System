package com.taskflow.approvalsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskFlow Approval System API")
                        .version("1.0.0")
                        .description("Backend API for managing request workflows with multi-level approvals"));
    }
}
