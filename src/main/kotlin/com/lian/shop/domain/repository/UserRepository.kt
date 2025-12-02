package com.lian.shop.domain.repository

import com.lian.shop.domain.User

interface UserRepository {
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun delete(user: User)
}

