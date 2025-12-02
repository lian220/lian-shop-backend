package com.lian.shop.domain.repository

import com.lian.shop.domain.OrderHistory

interface OrderHistoryRepository {
    fun save(history: OrderHistory): OrderHistory
    fun findById(id: Long): OrderHistory?
    fun findByOrderIdOrderByChangedAtDesc(orderId: Long): List<OrderHistory>
    fun findAll(): List<OrderHistory>
}

