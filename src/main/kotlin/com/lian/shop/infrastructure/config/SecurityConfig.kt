package com.lian.shop.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val environment: Environment) {

    @Value("\${CORS_ORIGIN:}") private var corsOrigin: String = ""

    @Value("\${CORS_ADDITIONAL_ORIGINS:}") private var additionalOrigins: String = ""

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 허용할 origin 목록
        val allowedOrigins = mutableListOf<String>()

        // 환경 변수에서 설정한 메인 origin 추가
        if (corsOrigin.isNotBlank()) {
            // 슬래시 제거
            val origin = corsOrigin.trimEnd('/')
            allowedOrigins.add(origin)
        }

        // 추가 origin들 (쉼표로 구분)
        if (additionalOrigins.isNotBlank()) {
            additionalOrigins.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                    origin ->
                val cleanOrigin = origin.trimEnd('/')
                if (cleanOrigin.isNotBlank()) {
                    allowedOrigins.add(cleanOrigin)
                }
            }
        }

        // 로컬 개발 환경 허용 (localhost, 127.0.0.1)
        // 프론트엔드 개발 서버
        allowedOrigins.add("http://localhost:3000")
        allowedOrigins.add("http://localhost:3001")
        allowedOrigins.add("http://127.0.0.1:3000")
        allowedOrigins.add("http://127.0.0.1:3001")

        // Swagger UI 접속용 (서버 자체 origin - 동적 포트)
        val serverPort = environment.getProperty("server.port", "8080")
        allowedOrigins.add("http://localhost:$serverPort")
        allowedOrigins.add("http://127.0.0.1:$serverPort")

        configuration.allowedOrigins = allowedOrigins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L // 1시간

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .csrf { it.disable() }
                .cors { it.configurationSource(corsConfigurationSource()) }
                .authorizeHttpRequests {
                    it.anyRequest()
                            .permitAll() // For MVP simplicity, we handle auth in service layer
                }
        return http.build()
    }
}
