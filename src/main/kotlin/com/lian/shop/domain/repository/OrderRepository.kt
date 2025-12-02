package com.lian.shop.domain.repository

import com.lian.shop.domain.Order
import com.lian.shop.domain.OrderStatus

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Long): Order?
    fun findByUserId(userId: Long): List<Order>
    fun findByOrderNumber(orderNumber: String): Order?
    fun findByStatus(status: OrderStatus): List<Order>
    fun findAll(): List<Order>
    fun delete(order: Order)
}

