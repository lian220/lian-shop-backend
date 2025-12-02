package com.lian.shop.repository

import com.lian.shop.domain.Order
import com.lian.shop.domain.OrderHistory
import com.lian.shop.domain.OrderItem
import com.lian.shop.domain.OrderStatus
import com.lian.shop.domain.Payment
import com.lian.shop.domain.PaymentCancel
import com.lian.shop.domain.PaymentHistory
import com.lian.shop.domain.Product
import com.lian.shop.domain.Shipment
import com.lian.shop.domain.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}

@Repository interface ProductRepository : JpaRepository<Product, Long>

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = ["user", "items", "items.product"])
    fun findByUserId(userId: Long): List<Order>
    fun findByOrderNumber(orderNumber: String): Order?
    fun findByStatus(status: OrderStatus): List<Order>
}

@Repository
interface OrderHistoryRepository : JpaRepository<OrderHistory, Long> {
    fun findByOrderIdOrderByChangedAtDesc(orderId: Long): List<OrderHistory>
}

@Repository interface OrderItemRepository : JpaRepository<OrderItem, Long>

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: Long): Payment?
    fun findByPaymentKey(paymentKey: String): Payment?
}

@Repository
interface PaymentHistoryRepository : JpaRepository<PaymentHistory, Long> {
    fun findByPaymentIdOrderByChangedAtDesc(paymentId: Long): List<PaymentHistory>
}

@Repository
interface PaymentCancelRepository : JpaRepository<PaymentCancel, Long> {
    fun findByPaymentIdOrderByCanceledAtDesc(paymentId: Long): List<PaymentCancel>
    fun findByTransactionKey(transactionKey: String): PaymentCancel?
}

@Repository
interface ShipmentRepository : JpaRepository<Shipment, Long> {
    fun findByOrderId(orderId: Long): Shipment?
}
