package com.lian.shop.domain

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
class OrderItem(
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") var order: Order,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") var product: Product,
        @Column(nullable = false) var quantity: Int,
        @Column(name = "price_at_purchase", nullable = false) var priceAtPurchase: BigDecimal
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}
