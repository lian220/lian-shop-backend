package com.lian.shop.infrastructure.repository

import com.lian.shop.domain.*
import com.lian.shop.domain.repository.*
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

// User Repository
@Repository
interface JpaUserRepository : JpaRepository<User, Long>, UserRepository {
    override fun findByEmail(email: String): User?
}

// Product Repository
@Repository
interface JpaProductRepository : JpaRepository<Product, Long>, ProductRepository

// Order Repository
@Repository
interface JpaOrderRepository : JpaRepository<Order, Long>, OrderRepository {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = ["user", "items", "items.product"])
    override fun findByUserId(userId: Long): List<Order>
    
    override fun findByOrderNumber(orderNumber: String): Order?
    override fun findByStatus(status: OrderStatus): List<Order>
}

// Order History Repository
@Repository
interface JpaOrderHistoryRepository : JpaRepository<OrderHistory, Long>, OrderHistoryRepository {
    override fun findByOrderIdOrderByChangedAtDesc(orderId: Long): List<OrderHistory>
}

// Order Item Repository
@Repository
interface JpaOrderItemRepository : JpaRepository<OrderItem, Long>, OrderItemRepository

// Payment Repository
@Repository
interface JpaPaymentRepository : JpaRepository<Payment, Long>, PaymentRepository {
    override fun findByOrderId(orderId: Long): Payment?
    override fun findByPaymentKey(paymentKey: String): Payment?
}

// Payment History Repository
@Repository
interface JpaPaymentHistoryRepository : JpaRepository<PaymentHistory, Long>, PaymentHistoryRepository {
    override fun findByPaymentIdOrderByChangedAtDesc(paymentId: Long): List<PaymentHistory>
}

// Payment Cancel Repository
@Repository
interface JpaPaymentCancelRepository : JpaRepository<PaymentCancel, Long>, PaymentCancelRepository {
    override fun findByPaymentIdOrderByCanceledAtDesc(paymentId: Long): List<PaymentCancel>
    override fun findByTransactionKey(transactionKey: String): PaymentCancel?
}

// Shipment Repository
@Repository
interface JpaShipmentRepository : JpaRepository<Shipment, Long>, ShipmentRepository {
    override fun findByOrderId(orderId: Long): Shipment?
}

