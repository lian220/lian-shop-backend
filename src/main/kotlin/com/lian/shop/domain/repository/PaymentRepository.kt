package com.lian.shop.domain.repository

import com.lian.shop.domain.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: Long): Payment?
    fun findByOrderId(orderId: Long): Payment?
    fun findByPaymentKey(paymentKey: String): Payment?
    fun findAll(): List<Payment>
    fun delete(payment: Payment)
}

