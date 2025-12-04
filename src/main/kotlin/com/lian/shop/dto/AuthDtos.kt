package com.lian.shop.dto

import com.lian.shop.domain.Role

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: Role = Role.CUSTOMER // Default to CUSTOMER, can be ADMIN
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String, // In real app use JWT, here we'll use simple ID/Role string
    val user: UserDto
)

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val role: Role
)
