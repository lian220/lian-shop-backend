package com.lian.shop.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "shipments")
class Shipment(
        @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") var order: Order,
        @Column(name = "tracking_number") var trackingNumber: String? = null,
        @Column(name = "carrier") var carrier: String? = null,
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var status: ShipmentStatus = ShipmentStatus.PREPARING,
        @Column(name = "shipped_at") var shippedAt: LocalDateTime? = null
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}

enum class ShipmentStatus {
    PREPARING,
    SHIPPED,
    DELIVERED
}
