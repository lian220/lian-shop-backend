package com.lian.shop.presentation.controller

import com.lian.shop.application.dto.ConfirmPaymentRequest
import com.lian.shop.application.dto.ConfirmPaymentResponse
import com.lian.shop.application.dto.PreparePaymentRequest
import com.lian.shop.application.dto.PreparePaymentResponse
import com.lian.shop.application.service.PaymentService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {
    /**
     * 결제 준비 API
     * 주문 정보를 전송하고 결제 URL 받기
     */
    @PostMapping("/prepare")
    fun preparePayment(@RequestBody request: PreparePaymentRequest): PreparePaymentResponse {
        return paymentService.preparePayment(request)
    }

    /**
     * 결제 승인 API
     * 프론트엔드에서 결제 성공 후 paymentKey를 받아서 서버에서 승인 처리
     */
    @PostMapping("/confirm")
    fun confirmPayment(@RequestBody request: ConfirmPaymentRequest): ConfirmPaymentResponse {
        return paymentService.confirmPayment(request)
    }

    /**
     * 테스트용 결제 승인 API
     * 실제 토스페이먼츠 API 호출 없이 결제 완료 처리
     */
    @PostMapping("/confirm/test")
    fun confirmPaymentForTest(@RequestBody request: ConfirmPaymentRequest): ConfirmPaymentResponse {
        return paymentService.confirmPaymentForTest(request)
    }
}

