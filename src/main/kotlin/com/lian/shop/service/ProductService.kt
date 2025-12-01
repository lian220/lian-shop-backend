package com.lian.shop.service

import com.lian.shop.domain.Product
import com.lian.shop.dto.CreateProductRequest
import com.lian.shop.dto.ProductDto
import com.lian.shop.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(private val productRepository: ProductRepository) {
    fun getAllProducts(): List<ProductDto> {
        return productRepository.findAll().map { it.toDto() }
    }

    fun getProduct(id: Long): ProductDto {
        val product =
                productRepository.findById(id).orElseThrow { RuntimeException("Product not found") }
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
