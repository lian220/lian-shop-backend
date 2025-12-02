package com.lian.shop.application.service

import com.lian.shop.infrastructure.external.tosspayments.TossPaymentsClient
import com.lian.shop.domain.*
import com.lian.shop.application.dto.ConfirmPaymentRequest
import com.lian.shop.application.dto.ConfirmPaymentResponse
import com.lian.shop.domain.repository.OrderRepository
import com.lian.shop.domain.repository.PaymentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@Transactional
class PaymentService(
    private val tossPaymentsClient: TossPaymentsClient,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) {
    @Value("\${TOSS_SECRET_KEY:}")
    private lateinit var secretKey: String

    /**
     * 토스페이먼츠 결제 승인 API 호출 및 주문 상태 업데이트
     */
    fun confirmPayment(request: ConfirmPaymentRequest): ConfirmPaymentResponse {
        if (secretKey.isBlank()) {
            throw IllegalStateException("토스페이먼츠 시크릿 키가 설정되지 않았습니다.")
        }

        // 주문 조회 (orderId는 토스페이먼츠 주문번호이므로 orderNumber로 조회)
        val order = orderRepository.findByOrderNumber(request.orderId)
            ?: throw RuntimeException("주문을 찾을 수 없습니다: ${request.orderId}")

        // 금액 검증
        val requestAmount = BigDecimal(request.amount)
        if (order.totalAmount.compareTo(requestAmount) != 0) {
            throw RuntimeException("결제 금액이 주문 금액과 일치하지 않습니다. 주문: ${order.totalAmount}, 결제: $requestAmount")
        }

        // Basic 인증 헤더 생성 (secretKey: 형식)
        val auth = Base64.getEncoder().encodeToString("$secretKey:".toByteArray())
        val authorization = "Basic $auth"

        val requestBody = mapOf(
            "paymentKey" to request.paymentKey,
            "orderId" to request.orderId,
            "amount" to request.amount
        )

        // Feign Client를 사용하여 API 호출
        val paymentResponse = try {
            tossPaymentsClient.confirmPayment(authorization, requestBody)
        } catch (e: Exception) {
            // 결제 실패 시 주문 상태 업데이트
            order.markAsPaymentFailed("결제 승인 실패: ${e.message}")
            orderRepository.save(order)
            throw RuntimeException("결제 승인 실패: ${e.message}", e)
        }

        // 응답 검증
        if (paymentResponse.status != "DONE" && paymentResponse.status != "READY") {
            order.markAsPaymentFailed("결제 승인 실패: ${paymentResponse.status}")
            orderRepository.save(order)
            throw RuntimeException("결제 승인 실패: ${paymentResponse.status}")
        }

        // paymentResponse는 이미 ConfirmPaymentResponse 타입이므로 그대로 사용

        // 결제 정보 저장
        val payment = savePayment(order, paymentResponse)
        
        // 결제 완료 시 주문 상태 업데이트
        if (paymentResponse.status == "DONE") {
            order.markAsPaid(payment, "SYSTEM")
            orderRepository.save(order)
        }

        return paymentResponse
    }

    /**
     * 결제 정보를 데이터베이스에 저장
     */
    private fun savePayment(order: Order, response: ConfirmPaymentResponse): Payment {
        val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
        
        val payment = Payment(
            order = order,
            paymentKey = response.paymentKey,
            orderIdToss = response.orderId,
            orderName = response.orderName,
            amount = BigDecimal(response.totalAmount),
            balanceAmount = BigDecimal(response.balanceAmount),
            suppliedAmount = response.suppliedAmount?.let { BigDecimal(it) },
            vat = response.vat?.let { BigDecimal(it) },
            taxFreeAmount = response.taxFreeAmount?.let { BigDecimal(it) },
            taxExemptionAmount = response.taxExemptionAmount?.let { BigDecimal(it) },
            status = when (response.status) {
                "DONE" -> PaymentStatus.DONE
                "CANCELED" -> PaymentStatus.CANCELED
                "PARTIAL_CANCELED" -> PaymentStatus.PARTIAL_CANCELED
                "ABORTED" -> PaymentStatus.ABORTED
                else -> PaymentStatus.READY
            },
            method = response.method,
            currency = response.currency,
            mId = response.mId,
            version = response.version,
            requestedAt = try {
                LocalDateTime.parse(response.requestedAt, dateFormatter)
            } catch (e: Exception) {
                null
            },
            approvedAt = response.approvedAt?.let {
                try {
                    LocalDateTime.parse(it, dateFormatter)
                } catch (e: Exception) {
                    null
                }
            },
            useEscrow = response.useEscrow,
            cultureExpense = response.cultureExpense
        )

        // 결제 이력 추가
        val history = PaymentHistory(
            payment = payment,
            previousStatus = null,
            newStatus = payment.status,
            reason = "결제 승인 완료",
            changedBy = "SYSTEM"
        )
        payment.histories.add(history)

        return paymentRepository.save(payment)
    }

    /**
     * 테스트용 결제 승인 (실제 토스페이먼츠 API 호출 없이 결제 완료 처리)
     */
    @Transactional
    fun confirmPaymentForTest(request: ConfirmPaymentRequest): ConfirmPaymentResponse {
        // 주문 조회
        val order = orderRepository.findByOrderNumber(request.orderId)
            ?: throw RuntimeException("주문을 찾을 수 없습니다: ${request.orderId}")

        // 금액 검증
        val requestAmount = BigDecimal(request.amount)
        if (order.totalAmount.compareTo(requestAmount) != 0) {
            throw RuntimeException("결제 금액이 주문 금액과 일치하지 않습니다. 주문: ${order.totalAmount}, 결제: $requestAmount")
        }

        // 테스트용 결제 응답 생성
        val now = LocalDateTime.now()
        val paymentResponse = ConfirmPaymentResponse(
            mId = "test_mid",
            version = "2022-11-16",
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            orderName = order.items.firstOrNull()?.product?.name ?: "테스트 주문",
            currency = "KRW",
            method = "카드",
            totalAmount = request.amount,
            balanceAmount = 0L,
            status = "DONE",
            requestedAt = now.format(DateTimeFormatter.ISO_DATE_TIME),
            approvedAt = now.format(DateTimeFormatter.ISO_DATE_TIME),
            suppliedAmount = (request.amount * 0.9).toLong(), // 공급가액 (부가세 제외)
            vat = (request.amount * 0.1).toLong(), // 부가세 10%
            useEscrow = false,
            cultureExpense = false,
            taxFreeAmount = 0L,
            taxExemptionAmount = 0L
        )

        // 결제 정보 저장
        val payment = savePayment(order, paymentResponse)
        
        // 결제 완료 시 주문 상태 업데이트
        order.markAsPaid(payment, "TEST")
        orderRepository.save(order)

        return paymentResponse
    }
}

