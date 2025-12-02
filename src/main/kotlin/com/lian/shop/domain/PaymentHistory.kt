package com.lian.shop.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 결제 상태 변경 이력
 * 결제의 모든 상태 변경을 추적합니다.
 */
@Entity
@Table(name = "payment_histories")
class PaymentHistory(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "payment_id", nullable = false)
    var payment: Payment,
    
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var previousStatus: PaymentStatus?,
    
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var newStatus: PaymentStatus,
    
    @Column(name = "changed_at", nullable = false)
    var changedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "changed_by", length = 100)
    var changedBy: String? = null, // 시스템, 사용자 ID 등
    
    @Column(name = "reason", columnDefinition = "TEXT")
    var reason: String? = null, // 상태 변경 사유
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String? = null // 추가 메타데이터 (JSON 형식)
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}

