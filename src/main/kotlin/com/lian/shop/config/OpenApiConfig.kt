package com.lian.shop.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Lian Shop API")
                    .description("Lian Shop 백엔드 API 문서")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Lian Shop")
                            .email("support@lianshop.com")
                    )
            )
    }
}

