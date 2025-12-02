package com.lian.shop.client

import com.lian.shop.dto.ConfirmPaymentRequest
import com.lian.shop.dto.ConfirmPaymentResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

/**
 * 토스페이먼츠 API 클라이언트
 * Feign Client를 사용하여 선언적으로 API 호출 정의
 */
@FeignClient(
    name = "tossPayments",
    url = "\${TOSS_BASE_URL:https://api.tosspayments.com}",
    configuration = [TossPaymentsClientConfig::class]
)
interface TossPaymentsClient {
    
    /**
     * 결제 승인 API 호출
     */
    @PostMapping(
        value = ["/v1/payments/confirm"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun confirmPayment(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: Map<String, Any>
    ): ConfirmPaymentResponse
}

