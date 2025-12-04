package com.lian.shop.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 정보
 * 결제와 연동되어 주문 상태가 자동으로 관리됩니다.
 */
@Entity
@Table(name = "orders")
class Order(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var status: OrderStatus,
    
    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal,
    
    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    var shippingAddress: String,
    
    @Column(name = "order_number", length = 100, unique = true)
    var orderNumber: String? = null, // 주문번호 (결제 시스템 orderId와 연동)
    
    @Column(name = "customer_name", length = 100)
    var customerName: String? = null, // 주문자명
    
    @Column(name = "customer_email", length = 255)
    var customerEmail: String? = null, // 주문자 이메일
    
    @Column(name = "customer_phone", length = 50)
    var customerPhone: String? = null, // 주문자 전화번호
    
    @Column(name = "payment_deadline")
    var paymentDeadline: LocalDateTime? = null, // 결제 기한 (가상계좌 등)
    
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null, // 취소 시간
    
    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    var cancelReason: String? = null, // 취소 사유
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String? = null // 추가 메타데이터 (JSON 형식)
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf()
    
    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var payment: Payment? = null
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var histories: MutableList<OrderHistory> = mutableListOf()
    
    /**
     * 주문 상태 변경
     */
    fun changeStatus(newStatus: OrderStatus, reason: String? = null, changedBy: String? = null) {
        val previousStatus = this.status
        this.status = newStatus
        this.updatedAt = LocalDateTime.now()
        
        if (newStatus == OrderStatus.CANCELLED) {
            this.cancelledAt = LocalDateTime.now()
            this.cancelReason = reason
        }
        
        // 이력 추가
        val history = OrderHistory(
            order = this,
            previousStatus = previousStatus,
            newStatus = newStatus,
            reason = reason,
            changedBy = changedBy
        )
        histories.add(history)
    }
    
    /**
     * 결제 완료 처리
     */
    fun markAsPaid(payment: Payment, changedBy: String? = null) {
        if (status == OrderStatus.PENDING || status == OrderStatus.PAYMENT_FAILED) {
            changeStatus(OrderStatus.PAID, "결제 완료", changedBy)
            this.payment = payment
        }
    }
    
    /**
     * 결제 실패 처리
     */
    fun markAsPaymentFailed(reason: String? = null, changedBy: String? = null) {
        if (status == OrderStatus.PENDING) {
            changeStatus(OrderStatus.PAYMENT_FAILED, reason ?: "결제 실패", changedBy)
        }
    }
    
    /**
     * 주문 취소 처리
     */
    fun cancel(reason: String, changedBy: String? = null) {
        if (status != OrderStatus.CANCELLED && status != OrderStatus.DELIVERED) {
            changeStatus(OrderStatus.CANCELLED, reason, changedBy)
        }
    }
}

enum class OrderStatus {
    PENDING,           // 결제 대기
    PAYMENT_FAILED,    // 결제 실패
    PAID,              // 결제 완료
    PREPARING,         // 상품 준비 중
    SHIPPED,           // 배송 중
    DELIVERED,         // 배송 완료
    CANCELLED,         // 주문 취소
    REFUNDED           // 환불 완료
}
