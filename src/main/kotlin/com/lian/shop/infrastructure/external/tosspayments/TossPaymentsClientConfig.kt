package com.lian.shop.infrastructure.external.tosspayments

import feign.Logger
import feign.RequestInterceptor
import feign.codec.ErrorDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Feign Client 설정
 */
@Configuration
class TossPaymentsClientConfig {
    
    /**
     * Feign 로깅 레벨 설정
     */
    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.BASIC
    }
    
    /**
     * 에러 디코더 (필요시 커스터마이징)
     */
    @Bean
    fun errorDecoder(): ErrorDecoder {
        return ErrorDecoder.Default()
    }
}

