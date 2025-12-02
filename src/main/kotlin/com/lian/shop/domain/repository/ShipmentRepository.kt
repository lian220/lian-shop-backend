package com.lian.shop.domain.repository

import com.lian.shop.domain.Shipment

interface ShipmentRepository {
    fun save(shipment: Shipment): Shipment
    fun findById(id: Long): Shipment?
    fun findByOrderId(orderId: Long): Shipment?
    fun findAll(): List<Shipment>
    fun delete(shipment: Shipment)
}

