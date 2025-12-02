package com.lian.shop.application.dto

import java.math.BigDecimal

data class OrderDto(
    val id: Long?,
    val userId: Long,
    val status: String,
    val totalAmount: BigDecimal,
    val shippingAddress: String,
    val orderNumber: String? = null,
    val createdAt: String? = null,
    val items: List<OrderItemDto>
)

data class OrderItemDto(
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val price: BigDecimal
)

data class CreateOrderRequest(
    val userId: Long,
    val items: List<CreateOrderItemRequest>,
    val shippingAddress: String,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null
)

data class CreateOrderItemRequest(val productId: Long, val quantity: Int)

