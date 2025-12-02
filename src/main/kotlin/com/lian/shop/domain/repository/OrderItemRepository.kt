package com.lian.shop.domain.repository

import com.lian.shop.domain.OrderItem

interface OrderItemRepository {
    fun save(item: OrderItem): OrderItem
    fun findById(id: Long): OrderItem?
    fun findAll(): List<OrderItem>
    fun delete(item: OrderItem)
}

