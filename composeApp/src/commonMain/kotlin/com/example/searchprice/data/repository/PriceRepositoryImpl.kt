package com.example.searchprice.data.repository

import com.example.searchprice.data.mapper.toProduct
import com.example.searchprice.data.source.RemotePriceDataSource
import com.example.searchprice.domain.model.Product
import com.example.searchprice.domain.repository.PriceRepository

class PriceRepositoryImpl(
    private val dataSource: RemotePriceDataSource
) : PriceRepository {

    override suspend fun searchProducts(
        query: String,
        latitude: Double,
        longitude: Double
    ): Result<List<Product>> = dataSource.searchPrices(query, latitude, longitude)
        .map { responses -> responses.mapNotNull { it.toProduct(latitude, longitude) } }
}
