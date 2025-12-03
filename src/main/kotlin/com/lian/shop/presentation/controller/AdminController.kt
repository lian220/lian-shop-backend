package com.lian.shop.presentation.controller

import com.lian.shop.application.dto.CreateProductRequest
import com.lian.shop.application.dto.ProductDto
import com.lian.shop.application.dto.OrderDto
import com.lian.shop.application.service.ProductService
import com.lian.shop.application.service.OrderService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val productService: ProductService,
    private val orderService: OrderService
) {
    
    /**
     * 관리자 전용: 모든 상품 조회 (관리용)
     */
    @GetMapping("/products")
    fun getAllProductsForAdmin(): List<ProductDto> {
        return productService.getAllProducts()
    }
    
    /**
     * 관리자 전용: 상품 생성
     */
    @PostMapping("/products")
    fun createProduct(@RequestBody request: CreateProductRequest): ProductDto {
        return productService.createProduct(request)
    }
    
    /**
     * 관리자 전용: 상품 수정
     */
    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request: CreateProductRequest
    ): ProductDto {
        return productService.updateProduct(id, request)
    }
    
    /**
     * 관리자 전용: 상품 삭제
     */
    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long) {
        productService.deleteProduct(id)
    }
    
    /**
     * 관리자 전용: 전체 주문 목록 조회
     */
    @GetMapping("/orders")
    fun getAllOrders(): List<OrderDto> {
        return orderService.getAllOrders()
    }
}

