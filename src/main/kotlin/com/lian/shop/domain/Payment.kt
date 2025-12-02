package com.lian.shop.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 정보
 * 토스페이먼츠 결제 승인 정보를 저장합니다.
 */
@Entity
@Table(name = "payments")
class Payment(
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", unique = true)
    var order: Order,
    
    @Column(name = "payment_key", length = 200, unique = true)
    var paymentKey: String? = null, // 토스페이먼츠 결제 키
    
    @Column(name = "order_id_toss", length = 100)
    var orderIdToss: String? = null, // 토스페이먼츠 주문번호
    
    @Column(name = "order_name", length = 255)
    var orderName: String? = null, // 주문명
    
    @Column(nullable = false)
    var amount: BigDecimal, // 결제 금액
    
    @Column(name = "balance_amount")
    var balanceAmount: BigDecimal? = null, // 잔액 (취소 후 남은 금액)
    
    @Column(name = "supplied_amount")
    var suppliedAmount: BigDecimal? = null, // 공급가액
    
    @Column(name = "vat")
    var vat: BigDecimal? = null, // 부가세
    
    @Column(name = "tax_free_amount")
    var taxFreeAmount: BigDecimal? = null, // 면세 금액
    
    @Column(name = "tax_exemption_amount")
    var taxExemptionAmount: BigDecimal? = null, // 과세 제외 금액
    
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var status: PaymentStatus, // 결제 상태
    
    @Column(name = "method", length = 50)
    var method: String? = null, // 결제 수단 (카드, 계좌이체, 가상계좌 등)
    
    @Column(name = "currency", length = 10)
    var currency: String? = null, // 통화 (KRW, USD 등)
    
    @Column(name = "m_id", length = 100)
    var mId: String? = null, // 상점 ID
    
    @Column(name = "version", length = 50)
    var version: String? = null, // API 버전
    
    @Column(name = "requested_at")
    var requestedAt: LocalDateTime? = null, // 결제 요청 시간
    
    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null, // 결제 승인 시간
    
    @Column(name = "use_escrow")
    var useEscrow: Boolean = false, // 에스크로 사용 여부
    
    @Column(name = "culture_expense")
    var cultureExpense: Boolean = false, // 문화비 지출 여부
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String? = null, // 추가 메타데이터 (JSON 형식)
    
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
    
    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], orphanRemoval = true)
    var histories: MutableList<PaymentHistory> = mutableListOf()
    
    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], orphanRemoval = true)
    var cancels: MutableList<PaymentCancel> = mutableListOf()
    
    /**
     * 총 취소 금액 계산
     */
    fun getTotalCancelAmount(): BigDecimal {
        return cancels
            .filter { it.cancelStatus == CancelStatus.DONE }
            .sumOf { it.cancelAmount }
    }
    
    /**
     * 취소 가능 금액 계산
     */
    fun getCancelableAmount(): BigDecimal {
        val totalCanceled = getTotalCancelAmount()
        return amount.subtract(totalCanceled)
    }
}

enum class PaymentStatus {
    READY,              // 결제 대기
    IN_PROGRESS,        // 결제 진행 중
    DONE,               // 결제 완료
    CANCELED,           // 전액 취소
    PARTIAL_CANCELED,   // 부분 취소
    ABORTED,            // 결제 실패
    EXPIRED             // 결제 만료
}
