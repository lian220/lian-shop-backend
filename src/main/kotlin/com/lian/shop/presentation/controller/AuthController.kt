package com.lian.shop.presentation.controller

import com.lian.shop.application.dto.AuthResponse
import com.lian.shop.application.dto.LoginRequest
import com.lian.shop.application.dto.SignupRequest
import com.lian.shop.application.service.AuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): AuthResponse {
        return authService.signup(request)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): AuthResponse {
        return authService.login(request)
    }
}
