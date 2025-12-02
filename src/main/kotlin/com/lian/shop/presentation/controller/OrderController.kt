package com.lian.shop.presentation.controller

import com.lian.shop.application.dto.CreateOrderRequest
import com.lian.shop.application.dto.OrderDto
import com.lian.shop.application.service.OrderService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): OrderDto {
        return orderService.createOrder(request)
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): OrderDto {
        return orderService.getOrder(id)
    }

    @GetMapping("/user/{userId}")
    fun getOrdersByUserId(@PathVariable userId: Long): List<OrderDto> {
        return orderService.getOrdersByUserId(userId)
    }
}
