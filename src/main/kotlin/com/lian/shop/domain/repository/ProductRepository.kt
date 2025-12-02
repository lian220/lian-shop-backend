package com.lian.shop.domain.repository

import com.lian.shop.domain.Product

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: Long): Product?
    fun findAll(): List<Product>
    fun delete(product: Product)
}

