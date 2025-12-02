package com.lian.shop.application.service

import com.lian.shop.domain.Product
import com.lian.shop.application.dto.CreateProductRequest
import com.lian.shop.application.dto.ProductDto
import com.lian.shop.domain.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(private val productRepository: ProductRepository) {
    fun getAllProducts(): List<ProductDto> {
        return productRepository.findAll().map { it.toDto() }
    }

    fun getProduct(id: Long): ProductDto {
        val product = productRepository.findById(id)
            ?: throw RuntimeException("Product not found")
        return product.toDto()
    }

    @Transactional
    fun createProduct(request: CreateProductRequest): ProductDto {
        val product =
                Product(
                        name = request.name,
                        description = request.description,
                        price = request.price,
                        stockQuantity = request.stockQuantity,
                        imageUrl = request.imageUrl
                )
        return productRepository.save(product).toDto()
    }

    @Transactional
    fun updateProduct(id: Long, request: CreateProductRequest): ProductDto {
        val product = productRepository.findById(id)
            ?: throw RuntimeException("Product not found")
        
        product.name = request.name
        product.description = request.description
        product.price = request.price
        product.stockQuantity = request.stockQuantity
        product.imageUrl = request.imageUrl
        
        return productRepository.save(product).toDto()
    }

    @Transactional
    fun deleteProduct(id: Long) {
        val product = productRepository.findById(id)
            ?: throw RuntimeException("Product not found")
        productRepository.delete(product)
    }

    private fun Product.toDto() =
            ProductDto(
                    id = id,
                    name = name,
                    description = description,
                    price = price,
                    stockQuantity = stockQuantity,
                    imageUrl = imageUrl
            )
}
