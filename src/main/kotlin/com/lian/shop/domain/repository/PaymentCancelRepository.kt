package com.lian.shop.domain.repository

import com.lian.shop.domain.PaymentCancel

interface PaymentCancelRepository {
    fun save(cancel: PaymentCancel): PaymentCancel
    fun findById(id: Long): PaymentCancel?
    fun findByPaymentIdOrderByCanceledAtDesc(paymentId: Long): List<PaymentCancel>
    fun findByTransactionKey(transactionKey: String): PaymentCancel?
    fun findAll(): List<PaymentCancel>
}

