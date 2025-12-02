package com.lian.shop.dto

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

