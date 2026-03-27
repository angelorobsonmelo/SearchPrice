package com.example.searchprice.domain.repository

import com.example.searchprice.domain.model.Product

interface PriceRepository {
    suspend fun searchProducts(
        query: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Product>>
}
