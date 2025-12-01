package com.lian.shop.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
        @Column(nullable = false, unique = true) var email: String,
        @Column(nullable = false) var password: String,
        @Column(nullable = false) var name: String,
        @Enumerated(EnumType.STRING) @Column(nullable = false) var role: Role = Role.CUSTOMER,
        @Column(name = "created_at") var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}

enum class Role {
    CUSTOMER,
    ADMIN
}
