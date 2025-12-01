package com.lian.shop.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class Payment(
        @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") var order: Order,
        @Column(name = "payment_key") var paymentKey: String? = null,
        @Column(nullable = false) var amount: BigDecimal,
        @Enumerated(EnumType.STRING) @Column(nullable = false) var status: PaymentStatus,
        @Column(name = "requested_at") var requestedAt: LocalDateTime? = null,
        @Column(name = "approved_at") var approvedAt: LocalDateTime? = null
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}

enum class PaymentStatus {
    READY,
    DONE,
    CANCELED
}
