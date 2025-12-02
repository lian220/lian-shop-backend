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

