package com.lian.shop.service

import com.lian.shop.domain.Order
import com.lian.shop.domain.OrderItem
import com.lian.shop.domain.OrderStatus
import com.lian.shop.dto.CreateOrderRequest
import com.lian.shop.dto.OrderDto
import com.lian.shop.dto.OrderItemDto
import com.lian.shop.repository.OrderRepository
import com.lian.shop.repository.ProductRepository
import com.lian.shop.repository.UserRepository
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
        val user =
                userRepository.findById(request.userId).orElseThrow {
                    RuntimeException("User not found")
                }

        val order =
                Order(
                        user = user,
                        status = OrderStatus.PENDING,
                        totalAmount = BigDecimal.ZERO, // Will calculate
                        shippingAddress = request.shippingAddress
                )

        var totalAmount = BigDecimal.ZERO
        val orderItems = mutableListOf<OrderItem>()

        for (itemReq in request.items) {
            val product =
                    productRepository.findById(itemReq.productId).orElseThrow {
                        RuntimeException("Product not found")
                    }

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

        return orderRepository.save(order).toDto()
    }

    fun getOrder(id: Long): OrderDto {
        return orderRepository
                .findById(id)
                .orElseThrow { RuntimeException("Order not found") }
                .toDto()
    }

    private fun Order.toDto() =
            OrderDto(
                    id = id,
                    userId = user.id!!,
                    status = status.name,
                    totalAmount = totalAmount,
                    shippingAddress = shippingAddress,
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
