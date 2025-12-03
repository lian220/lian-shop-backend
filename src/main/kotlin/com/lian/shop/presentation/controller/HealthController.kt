package com.lian.shop.presentation.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Health Check 엔드포인트
 * 
 * 백엔드 서버의 상태를 확인하고 콜드 스타트를 방지하기 위한 엔드포인트입니다.
 * 외부 모니터링 서비스(UptimeRobot, Uptime Monitor 등)에서 주기적으로 호출하여
 * 서버가 sleep 상태로 들어가는 것을 방지할 수 있습니다.
 */
@RestController
@RequestMapping("/api/health")
class HealthController {
    
    /**
     * 간단한 Health Check
     * 
     * @return 서버 상태 정보
     */
    @GetMapping
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now().toString(),
            "message" to "Server is running"
        )
    }
    
    /**
     * Ping 엔드포인트 (최소한의 응답)
     * 
     * @return "pong" 문자열
     */
    @GetMapping("/ping")
    fun ping(): String {
        return "pong"
    }
}

