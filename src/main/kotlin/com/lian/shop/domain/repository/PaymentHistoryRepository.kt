package com.lian.shop.domain.repository

import com.lian.shop.domain.PaymentHistory

interface PaymentHistoryRepository {
    fun save(history: PaymentHistory): PaymentHistory
    fun findById(id: Long): PaymentHistory?
    fun findByPaymentIdOrderByChangedAtDesc(paymentId: Long): List<PaymentHistory>
    fun findAll(): List<PaymentHistory>
}

