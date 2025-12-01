package com.lian.shop.service

import com.lian.shop.domain.User
import com.lian.shop.dto.AuthResponse
import com.lian.shop.dto.LoginRequest
import com.lian.shop.dto.SignupRequest
import com.lian.shop.dto.UserDto
import com.lian.shop.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Email already exists")
        }

        val user =
                User(
                        email = request.email,
                        password = passwordEncoder.encode(request.password),
                        name = request.name,
                        role = request.role
                )

        val savedUser = userRepository.save(user)
        return generateAuthResponse(savedUser)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user =
                userRepository.findByEmail(request.email)
                        ?: throw RuntimeException("User not found")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw RuntimeException("Invalid password")
        }

        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        // In a real app, generate a JWT here.
        // For this MVP, we'll return a simple base64 string of "id:role"
        val token =
                java.util.Base64.getEncoder()
                        .encodeToString("${user.id}:${user.role}".toByteArray())

        return AuthResponse(
                token = token,
                user =
                        UserDto(
                                id = user.id!!,
                                email = user.email,
                                name = user.name,
                                role = user.role
                        )
        )
    }
}
