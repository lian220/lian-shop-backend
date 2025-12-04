package com.lian.shop.repository

import com.lian.shop.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}

@Repository interface ProductRepository : JpaRepository<Product, Long>

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByUserId(userId: Long): List<Order>
}

@Repository interface OrderItemRepository : JpaRepository<OrderItem, Long>

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: Long): Payment?
}

@Repository
interface ShipmentRepository : JpaRepository<Shipment, Long> {
    fun findByOrderId(orderId: Long): Shipment?
}
