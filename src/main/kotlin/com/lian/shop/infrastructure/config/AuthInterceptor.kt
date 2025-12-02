package com.lian.shop.infrastructure.config

import com.lian.shop.application.service.TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(private val tokenService: TokenService) : HandlerInterceptor {
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // OPTIONS 요청은 통과 (CORS preflight)
        if (request.method == "OPTIONS") {
            return true
        }
        
        val path = request.requestURI
        
        // 관리자 전용 경로 체크
        if (path.startsWith("/api/admin")) {
            val token = tokenService.extractToken(request.getHeader("Authorization"))
            
            if (!tokenService.isAdmin(token)) {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.contentType = "application/json"
                response.writer.write("""{"error":"관리자 권한이 필요합니다."}""")
                return false
            }
        }
        
        // 인증이 필요한 경로 (선택사항)
        // 예: /api/orders (주문 생성 등)
        
        return true
    }
}

