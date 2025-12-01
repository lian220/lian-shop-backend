package com.lian.shop.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class Product(
        @Column(nullable = false) var name: String,
        @Column(columnDefinition = "TEXT") var description: String? = null,
        @Column(nullable = false) var price: BigDecimal,
        @Column(name = "stock_quantity", nullable = false) var stockQuantity: Int,
        @Column(name = "image_url") var imageUrl: String? = null,
        @Column(name = "created_at") var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}
