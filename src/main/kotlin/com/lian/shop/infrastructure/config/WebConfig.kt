package com.lian.shop.infrastructure.config

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
                "/api/payments/confirm", // 결제 승인은 인증 제외 (프론트엔드에서 결제 후 호출)
                "/api/payments/confirm/test", // 테스트용 결제 승인도 인증 제외
                "/api-docs/**",  // API 문서는 제외
                "/swagger-ui/**" // Swagger UI는 제외
            )
    }
    
    // RestTemplate Bean 제거 - Feign Client 사용으로 대체
}

