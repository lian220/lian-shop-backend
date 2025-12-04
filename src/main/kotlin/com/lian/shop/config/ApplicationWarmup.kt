package com.lian.shop.config

import com.lian.shop.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture

@Component
class ApplicationWarmup(
    private val productService: ProductService,
    private val environment: Environment
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ApplicationWarmup::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("🔥 애플리케이션 Warm-up 시작...")
        
        // 비동기로 warm up 수행 (애플리케이션 시작 속도에 영향 없음)
        CompletableFuture.runAsync {
            try {
                // 1. 데이터베이스 연결 및 JPA 초기화
                logger.info("📊 데이터베이스 연결 및 JPA 초기화 중...")
                productService.getAllProducts()
                logger.info("✅ 데이터베이스 연결 및 JPA 초기화 완료")
                
                // 2. HTTP 엔드포인트 warm up
                warmupHttpEndpoints()
                
                // 3. JVM 힙 메모리 warm up
                logger.info("💾 JVM 힙 메모리 warm up 중...")
                System.gc() // 가비지 컬렉션으로 메모리 정리
                logger.info("✅ JVM 힙 메모리 warm up 완료")
                
                logger.info("🎉 애플리케이션 Warm-up 완료!")
            } catch (e: Exception) {
                logger.warn("⚠️ Warm-up 중 오류 발생 (무시 가능): ${e.message}")
                // Warm-up 실패해도 애플리케이션은 정상 실행됨
            }
        }
    }
    
    private fun warmupHttpEndpoints() {
        try {
            val serverPort = environment.getProperty("server.port", "8080")
            val baseUrl = "http://localhost:$serverPort"
            val restTemplate = RestTemplate()
            
            logger.info("🌐 HTTP 엔드포인트 warm up 중...")
            
            // 주요 엔드포인트들 warm up
            val endpoints = listOf(
                "/api/products",
                "/api-docs",
                "/swagger-ui.html"
            )
            
            endpoints.forEach { endpoint ->
                try {
                    restTemplate.getForObject("$baseUrl$endpoint", String::class.java)
                    logger.debug("✅ $endpoint warm up 완료")
                } catch (e: Exception) {
                    logger.debug("⚠️ $endpoint warm up 실패 (무시 가능): ${e.message}")
                }
            }
            
            logger.info("✅ HTTP 엔드포인트 warm up 완료")
        } catch (e: Exception) {
            logger.warn("⚠️ HTTP 엔드포인트 warm up 중 오류 (무시 가능): ${e.message}")
        }
    }
}

