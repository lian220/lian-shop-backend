package com.lian.shop.dto

import java.math.BigDecimal

data class ProductDto(
        val id: Long?,
        val name: String,
        val description: String?,
        val price: BigDecimal,
        val stockQuantity: Int,
        val imageUrl: String?
)

data class CreateProductRequest(
        val name: String,
        val description: String?,
        val price: BigDecimal,
        val stockQuantity: Int,
        val imageUrl: String?
)

data class OrderDto(
        val id: Long?,
        val userId: Long,
        val status: String,
        val totalAmount: BigDecimal,
        val shippingAddress: String,
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
        val shippingAddress: String
)

data class CreateOrderItemRequest(val productId: Long, val quantity: Int)
