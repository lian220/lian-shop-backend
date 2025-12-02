package com.lian.shop.application.service

import com.lian.shop.domain.Role
import com.lian.shop.domain.User
import com.lian.shop.domain.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class TokenService(private val userRepository: UserRepository) {
    
    /**
     * 토큰에서 사용자 정보 추출
     * 토큰 형식: base64("id:role")
     */
    fun validateToken(token: String): User? {
        return try {
            val decoded = String(java.util.Base64.getDecoder().decode(token))
            val parts = decoded.split(":")
            if (parts.size != 2) return null
            
            val userId = parts[0].toLongOrNull() ?: return null
            val role = try {
                Role.valueOf(parts[1])
            } catch (e: IllegalArgumentException) {
                return null
            }
            
            // DB에서 사용자 확인 (보안 강화)
            val user = userRepository.findById(userId)
            if (user == null || user.role != role) {
                return null
            }
            
            user
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 관리자 권한 확인
     */
    fun isAdmin(token: String?): Boolean {
        if (token == null) return false
        val user = validateToken(token)
        return user?.role == Role.ADMIN
    }
    
    /**
     * 요청 헤더에서 토큰 추출
     */
    fun extractToken(authorizationHeader: String?): String? {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null
        }
        return authorizationHeader.substring(7)
    }
}

