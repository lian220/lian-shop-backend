package com.lian.shop.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(private val authInterceptor: AuthInterceptor) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**",  // 인증 관련은 제외
                "/api/products", // 상품 조회는 제외
                "/api/products/**", // 상품 상세 조회는 제외
                "/api-docs/**",  // API 문서는 제외
                "/swagger-ui/**" // Swagger UI는 제외
            )
    }
}

