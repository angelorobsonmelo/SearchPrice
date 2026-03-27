package com.example.searchprice.domain.usecase

import com.example.searchprice.domain.model.Product
import com.example.searchprice.domain.repository.PriceRepository
import com.example.searchprice.domain.util.SortOption

class SearchProductsUseCase(private val repository: PriceRepository) {

    suspend operator fun invoke(
        query: String,
        latitude: Double,
        longitude: Double,
        sortOption: SortOption = SortOption.PRICE
    ): Result<List<Product>> = repository.searchProducts(query, latitude, longitude)
        .map { products ->
            when (sortOption) {
                SortOption.PRICE -> products.sortedBy { it.price }
                SortOption.DISTANCE -> products.sortedBy { it.distanceKm }
            }
        }
}
