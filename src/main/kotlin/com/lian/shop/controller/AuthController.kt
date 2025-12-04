package com.lian.shop.controller

import com.lian.shop.dto.AuthResponse
import com.lian.shop.dto.LoginRequest
import com.lian.shop.dto.SignupRequest
import com.lian.shop.service.AuthService
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
