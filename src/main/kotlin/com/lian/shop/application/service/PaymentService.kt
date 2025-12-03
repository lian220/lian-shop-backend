package com.lian.shop.application.service

import com.lian.shop.infrastructure.external.naverpay.NaverPayClient
import com.lian.shop.domain.*
import com.lian.shop.application.dto.ConfirmPaymentRequest
import com.lian.shop.application.dto.ConfirmPaymentResponse
import com.lian.shop.application.dto.PreparePaymentRequest
import com.lian.shop.application.dto.PreparePaymentResponse
import com.lian.shop.domain.repository.OrderRepository
import com.lian.shop.domain.repository.PaymentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class PaymentService(
    private val naverPayClient: NaverPayClient,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) {
    @Value("\${NAVER_PAY_CLIENT_ID:}")
    private lateinit var clientId: String
    
    @Value("\${NAVER_PAY_CLIENT_SECRET:}")
    private lateinit var clientSecret: String

    /**
     * 네이버페이 결제 준비 API 호출
     * 주문 정보를 전송하고 결제 URL 받기
     * 
     * 현재는 개발 환경이므로 항상 테스트 모드로 작동
     */
    fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse {
        // 주문 조회 (금액 검증용)
        val order = orderRepository.findByOrderNumber(request.orderId)
            ?: throw RuntimeException("주문을 찾을 수 없습니다: ${request.orderId}")
        
        // 금액 검증 (소수점 차이 허용 - 소수점 2자리까지만 비교)
        val requestAmount = BigDecimal(request.amount).setScale(2, java.math.RoundingMode.HALF_UP)
        val orderAmount = order.totalAmount.setScale(2, java.math.RoundingMode.HALF_UP)
        if (orderAmount.compareTo(requestAmount) != 0) {
            throw RuntimeException("결제 금액이 주문 금액과 일치하지 않습니다. 주문: $orderAmount, 결제: $requestAmount")
        }
        
        // TODO: 프로덕션 환경에서는 실제 네이버페이 API 호출
        // 현재는 테스트 모드 - 바로 성공 URL로 리다이렉트
        val testPaymentKey = "test_payment_${System.currentTimeMillis()}"
        return PreparePaymentResponse(
            orderId = request.orderId,
            orderName = request.orderName,
            amount = request.amount,
            paymentUrl = "${request.successUrl}?orderId=${request.orderId}&paymentKey=$testPaymentKey&amount=${request.amount}"
        )
        
        /* 실제 네이버페이 API 호출 코드 (프로덕션용)
        // 네이버페이 결제 준비 요청
        val requestBody = mapOf(
            "orderId" to request.orderId,
            "productName" to request.orderName,
            "totalPayAmount" to request.amount,
            "returnUrl" to request.successUrl,
            "failUrl" to request.failUrl
        )

        // Feign Client를 사용하여 API 호출
        val naverPayResponse = try {
            naverPayClient.reservePayment(clientId, clientSecret, requestBody)
        } catch (e: Exception) {
            throw RuntimeException("결제 준비 실패: ${e.message}", e)
        }

        // 응답 검증
        if (naverPayResponse.code != "Success") {
            throw RuntimeException("결제 준비 실패: ${naverPayResponse.message}")
        }

        val body = naverPayResponse.body ?: throw RuntimeException("결제 응답 본문이 없습니다")

        return PreparePaymentResponse(
            orderId = request.orderId,
            orderName = request.orderName,
            amount = request.amount,
            paymentUrl = body.paymentUrl
        )
        */
    }

    /**
     * 네이버페이 결제 승인 API 호출 및 주문 상태 업데이트
     */
    fun confirmPayment(request: ConfirmPaymentRequest): ConfirmPaymentResponse {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw IllegalStateException("네이버페이 클라이언트 정보가 설정되지 않았습니다.")
        }

        // 주문 조회 (orderId는 네이버페이 주문번호이므로 orderNumber로 조회)
        val order = orderRepository.findByOrderNumber(request.orderId)
            ?: throw RuntimeException("주문을 찾을 수 없습니다: ${request.orderId}")

        // 금액 검증
        val requestAmount = BigDecimal(request.amount)
        if (order.totalAmount.compareTo(requestAmount) != 0) {
            throw RuntimeException("결제 금액이 주문 금액과 일치하지 않습니다. 주문: ${order.totalAmount}, 결제: $requestAmount")
        }

        // 네이버페이 결제 승인 요청 바디
        val requestBody = mapOf(
            "paymentId" to request.paymentKey,  // 네이버페이에서는 paymentId 사용
            "detail" to mapOf(
                "productName" to (order.items.firstOrNull()?.product?.name ?: "주문 상품"),
                "totalPayAmount" to request.amount
            )
        )

        // Feign Client를 사용하여 API 호출
        val naverPayResponse = try {
            naverPayClient.approvePayment(clientId, clientSecret, requestBody)
        } catch (e: Exception) {
            // 결제 실패 시 주문 상태 업데이트
            order.markAsPaymentFailed("결제 승인 실패: ${e.message}")
            orderRepository.save(order)
            throw RuntimeException("결제 승인 실패: ${e.message}", e)
        }

        // 응답 검증 - 네이버페이는 code가 "Success"이면 성공
        if (naverPayResponse.code != "Success") {
            order.markAsPaymentFailed("결제 승인 실패: ${naverPayResponse.message}")
            orderRepository.save(order)
            throw RuntimeException("결제 승인 실패: ${naverPayResponse.message}")
        }

        // 네이버페이 응답을 공통 포맷으로 변환
        val paymentResponse = convertNaverPayResponse(naverPayResponse, request)

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
     * 네이버페이 응답을 공통 ConfirmPaymentResponse 포맷으로 변환
     */
    private fun convertNaverPayResponse(
        naverResponse: com.lian.shop.infrastructure.external.naverpay.NaverPaymentResponse,
        request: ConfirmPaymentRequest
    ): ConfirmPaymentResponse {
        val body = naverResponse.body ?: throw RuntimeException("결제 응답 본문이 없습니다")
        
        return ConfirmPaymentResponse(
            mId = "naver_pay",
            version = "1.0",
            paymentKey = body.paymentId,
            orderId = request.orderId,
            orderName = body.productName,
            currency = "KRW",
            method = "네이버페이",
            totalAmount = body.totalPayAmount,
            balanceAmount = body.totalPayAmount,
            status = if (body.admissionState == "SUCCESS") "DONE" else "READY",
            requestedAt = body.admissionYmdt,
            approvedAt = body.admissionYmdt,
            suppliedAmount = body.taxScopeAmount ?: (body.totalPayAmount * 0.9).toLong(),
            vat = (body.totalPayAmount * 0.1).toLong(),
            useEscrow = false,
            cultureExpense = false,
            taxFreeAmount = body.taxExScopeAmount ?: 0L,
            taxExemptionAmount = 0L
        )
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
     * 테스트용 결제 승인 (실제 네이버페이 API 호출 없이 결제 완료 처리)
     */
    @Transactional
    fun confirmPaymentForTest(request: ConfirmPaymentRequest): ConfirmPaymentResponse {
        // 주문 조회
        val order = orderRepository.findByOrderNumber(request.orderId)
            ?: throw RuntimeException("주문을 찾을 수 없습니다: ${request.orderId}")

        // 금액 검증 (소수점 차이 허용 - 소수점 2자리까지만 비교)
        val requestAmount = BigDecimal(request.amount).setScale(2, java.math.RoundingMode.HALF_UP)
        val orderAmount = order.totalAmount.setScale(2, java.math.RoundingMode.HALF_UP)
        if (orderAmount.compareTo(requestAmount) != 0) {
            throw RuntimeException("결제 금액이 주문 금액과 일치하지 않습니다. 주문: $orderAmount, 결제: $requestAmount")
        }

        // 테스트용 결제 응답 생성
        val now = LocalDateTime.now()
        val paymentResponse = ConfirmPaymentResponse(
            mId = "naver_pay_test",
            version = "1.0",
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            orderName = order.items.firstOrNull()?.product?.name ?: "테스트 주문",
            currency = "KRW",
            method = "네이버페이",
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

