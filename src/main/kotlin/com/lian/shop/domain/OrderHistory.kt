package com.lian.shop.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 주문 상태 변경 이력
 * 주문의 모든 상태 변경을 추적합니다.
 */
@Entity
@Table(name = "order_histories")
class OrderHistory(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false)
    var order: Order,
    
    @Enumerated(EnumType.STRING) @Column(name = "previous_status")
    var previousStatus: OrderStatus?,
    
    @Enumerated(EnumType.STRING) @Column(name = "new_status", nullable = false)
    var newStatus: OrderStatus,
    
    @Column(name = "changed_at", nullable = false)
    var changedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "changed_by", length = 100)
    var changedBy: String? = null, // 시스템, 사용자 ID 등
    
    @Column(name = "reason", columnDefinition = "TEXT")
    var reason: String? = null, // 상태 변경 사유
    
    @Column(name = "payment_id")
    var paymentId: Long? = null, // 결제와 연관된 경우 결제 ID
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String? = null // 추가 메타데이터 (JSON 형식)
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}

