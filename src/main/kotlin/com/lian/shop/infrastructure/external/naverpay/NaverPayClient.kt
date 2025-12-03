package com.lian.shop.infrastructure.external.naverpay

import com.lian.shop.application.dto.ConfirmPaymentResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * 네이버페이 API 클라이언트
 * Feign Client를 사용하여 선언적으로 API 호출 정의
 */
@FeignClient(
    name = "naverPay",
    url = "\${NAVER_PAY_BASE_URL:https://dev.apis.naver.com/naverpay-partner/naverpay}",
    configuration = [NaverPayClientConfig::class]
)
interface NaverPayClient {
    
    /**
     * 결제 준비 API
     * 주문 정보를 전송하고 결제 URL 받기
     */
    @PostMapping(
        value = ["/payments/v1/reserve"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun reservePayment(
        @RequestHeader("X-Naver-Client-Id") clientId: String,
        @RequestHeader("X-Naver-Client-Secret") clientSecret: String,
        @RequestBody request: Map<String, Any>
    ): NaverPaymentReserveResponse
    
    /**
     * 결제 승인 API 호출
     * 네이버페이는 결제 완료 후 승인 처리
     */
    @PostMapping(
        value = ["/payments/v2.2/apply/payment"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun approvePayment(
        @RequestHeader("X-Naver-Client-Id") clientId: String,
        @RequestHeader("X-Naver-Client-Secret") clientSecret: String,
        @RequestBody request: Map<String, Any>
    ): NaverPaymentResponse
    
    /**
     * 결제 조회 API
     */
    @GetMapping(
        value = ["/payments/v1/list"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getPaymentInfo(
        @RequestHeader("X-Naver-Client-Id") clientId: String,
        @RequestHeader("X-Naver-Client-Secret") clientSecret: String,
        @RequestParam("paymentId") paymentId: String
    ): NaverPaymentResponse
}

/**
 * 네이버페이 결제 준비 응답 DTO
 */
data class NaverPaymentReserveResponse(
    val code: String,
    val message: String? = null,
    val body: NaverPaymentReserveBody? = null
)

data class NaverPaymentReserveBody(
    val reserveId: String,  // 예약 ID
    val paymentUrl: String  // 결제 URL
)

/**
 * 네이버페이 결제 응답 DTO
 */
data class NaverPaymentResponse(
    val code: String,
    val message: String? = null,
    val body: NaverPaymentBody? = null
)

data class NaverPaymentBody(
    val paymentId: String,
    val productName: String,
    val totalPayAmount: Long,
    val primaryPayAmount: Long,
    val npointPayAmount: Long? = null,
    val giftCardPayAmount: Long? = null,
    val taxScopeAmount: Long? = null,
    val taxExScopeAmount: Long? = null,
    val environmentDepositAmount: Long? = null,
    val paymentId1: String? = null,
    val admissionYmdt: String,
    val admissionState: String
)

