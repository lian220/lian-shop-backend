package com.lian.shop.application.service

import com.lian.shop.domain.Order
import com.lian.shop.domain.OrderItem
import com.lian.shop.domain.OrderStatus
import com.lian.shop.application.dto.CreateOrderRequest
import com.lian.shop.application.dto.OrderDto
import com.lian.shop.application.dto.OrderItemDto
import com.lian.shop.domain.repository.OrderRepository
import com.lian.shop.domain.repository.ProductRepository
import com.lian.shop.domain.repository.UserRepository
import java.math.BigDecimal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderService(
        private val orderRepository: OrderRepository,
        private val userRepository: UserRepository,
        private val productRepository: ProductRepository
) {
    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderDto {
        val user = userRepository.findById(request.userId)
            ?: throw RuntimeException("User not found")

        // 주문번호 생성 (토스페이먼츠 orderId로 사용)
        val orderNumber = generateOrderNumber()

        val order =
                Order(
                        user = user,
                        status = OrderStatus.PENDING,
                        totalAmount = BigDecimal.ZERO, // Will calculate
                        shippingAddress = request.shippingAddress,
                        orderNumber = orderNumber,
                        customerName = request.customerName,
                        customerEmail = request.customerEmail,
                        customerPhone = request.customerPhone
                )

        var totalAmount = BigDecimal.ZERO
        val orderItems = mutableListOf<OrderItem>()

        for (itemReq in request.items) {
            val product = productRepository.findById(itemReq.productId)
                ?: throw RuntimeException("Product not found")

            if (product.stockQuantity < itemReq.quantity) {
                throw RuntimeException("Not enough stock for product ${product.name}")
            }
            product.stockQuantity -= itemReq.quantity

            val orderItem =
                    OrderItem(
                            order = order,
                            product = product,
                            quantity = itemReq.quantity,
                            priceAtPurchase = product.price
                    )
            orderItems.add(orderItem)
            totalAmount = totalAmount.add(product.price.multiply(BigDecimal(itemReq.quantity)))
        }

        order.items = orderItems
        order.totalAmount = totalAmount

        val savedOrder = orderRepository.save(order)
        
        // 주문 생성 이력 추가
        savedOrder.changeStatus(OrderStatus.PENDING, "주문 생성", "SYSTEM")
        orderRepository.save(savedOrder)

        return savedOrder.toDto()
    }
    
    /**
     * 주문번호 생성 (토스페이먼츠 orderId 형식)
     */
    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "order_${timestamp}_${random}"
    }

    fun getOrder(id: Long): OrderDto {
        val order = orderRepository.findById(id)
            ?: throw RuntimeException("Order not found")
        return order.toDto()
    }

    fun getOrdersByUserId(userId: Long): List<OrderDto> {
        return orderRepository
                .findByUserId(userId)
                .map { it.toDto() }
                .sortedByDescending { it.id }
    }
    
    /**
     * 전체 주문 목록 조회 (관리자용)
     */
    fun getAllOrders(): List<OrderDto> {
        return orderRepository
                .findAll()
                .map { it.toDto() }
                .sortedByDescending { it.id }
    }

    private fun Order.toDto() =
            OrderDto(
                    id = id,
                    userId = user.id!!,
                    status = status.name,
                    totalAmount = totalAmount,
                    shippingAddress = shippingAddress,
                    orderNumber = orderNumber,
                    createdAt = createdAt?.toString(),
                    items =
                            items.map {
                                OrderItemDto(
                                        productId = it.product.id!!,
                                        productName = it.product.name,
                                        quantity = it.quantity,
                                        price = it.priceAtPurchase
                                )
                            }
            )
}
