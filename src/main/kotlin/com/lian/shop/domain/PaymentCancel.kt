package com.lian.shop.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 취소/환불 이력
 * 부분 취소를 지원하며, 각 취소 건마다 별도 레코드로 관리합니다.
 */
@Entity
@Table(name = "payment_cancels")
class PaymentCancel(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "payment_id", nullable = false)
    var payment: Payment,
    
    @Column(name = "cancel_amount", nullable = false)
    var cancelAmount: BigDecimal, // 취소 금액
    
    @Column(name = "cancel_reason", nullable = false, columnDefinition = "TEXT")
    var cancelReason: String, // 취소 사유
    
    @Column(name = "tax_free_amount")
    var taxFreeAmount: BigDecimal? = null, // 면세 금액
    
    @Column(name = "tax_exemption_amount")
    var taxExemptionAmount: BigDecimal? = null, // 과세 제외 금액
    
    @Column(name = "refundable_amount")
    var refundableAmount: BigDecimal? = null, // 환불 가능 금액
    
    @Column(name = "transaction_key", length = 100)
    var transactionKey: String? = null, // 결제 시스템 거래 키
    
    @Column(name = "receipt_key", length = 100)
    var receiptKey: String? = null, // 결제 시스템 영수증 키
    
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var cancelStatus: CancelStatus, // 취소 상태
    
    @Column(name = "cancel_request_id", length = 100)
    var cancelRequestId: String? = null, // 멱등성을 위한 취소 요청 ID
    
    @Column(name = "canceled_at", nullable = false)
    var canceledAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "refund_receive_account", columnDefinition = "TEXT")
    var refundReceiveAccount: String? = null, // 환불 계좌 정보 (JSON 형식)
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String? = null // 추가 메타데이터 (JSON 형식)
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
}

enum class CancelStatus {
    IN_PROGRESS, // 취소 진행 중
    DONE,        // 취소 완료
    ABORTED      // 취소 실패
}

