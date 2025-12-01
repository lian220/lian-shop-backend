package com.lian.shop.controller

import com.lian.shop.dto.CreateOrderRequest
import com.lian.shop.dto.OrderDto
import com.lian.shop.service.OrderService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = ["*"])
class OrderController(private val orderService: OrderService) {
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): OrderDto {
        return orderService.createOrder(request)
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): OrderDto {
        return orderService.getOrder(id)
    }
}
