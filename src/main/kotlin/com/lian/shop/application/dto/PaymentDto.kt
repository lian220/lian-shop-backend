package com.lian.shop.application.dto

// 결제 준비 요청
data class PreparePaymentRequest(
    val orderId: String,
    val amount: Long,
    val orderName: String,
    val successUrl: String,
    val failUrl: String
)

// 결제 준비 응답
data class PreparePaymentResponse(
    val orderId: String,
    val orderName: String,
    val amount: Long,
    val paymentUrl: String  // 네이버페이 결제 URL
)

// 결제 승인 요청
data class ConfirmPaymentRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Long
)

// 결제 승인 응답
data class ConfirmPaymentResponse(
    val mId: String,
    val version: String,
    val paymentKey: String,
    val orderId: String,
    val orderName: String,
    val currency: String,
    val method: String,
    val totalAmount: Long,
    val balanceAmount: Long,
    val status: String,
    val requestedAt: String,
    val approvedAt: String?,
    val suppliedAmount: Long?,
    val vat: Long?,
    val useEscrow: Boolean,
    val cultureExpense: Boolean,
    val taxFreeAmount: Long?,
    val taxExemptionAmount: Long?
)

