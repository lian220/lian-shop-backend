package com.lian.shop.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") var user: User,
        @Enumerated(EnumType.STRING) @Column(nullable = false) var status: OrderStatus,
        @Column(name = "total_amount", nullable = false) var totalAmount: BigDecimal,
        @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
        var shippingAddress: String,
        @Column(name = "created_at") var createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf()
}

enum class OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
