package com.lian.shop.controller

import com.lian.shop.dto.CreateProductRequest
import com.lian.shop.dto.ProductDto
import com.lian.shop.service.ProductService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {
    @GetMapping
    fun getAllProducts(): List<ProductDto> {
        return productService.getAllProducts()
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ProductDto {
        return productService.getProduct(id)
    }

    // 상품 생성은 관리자 전용이므로 AdminController로 이동
    // @PostMapping
    // fun createProduct(@RequestBody request: CreateProductRequest): ProductDto {
    //     return productService.createProduct(request)
    // }
}
