package com.example.searchprice.domain.model

data class Product(
    val barcode: String,
    val name: String,
    val price: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val storeName: String,
    val storeFantasyName: String?,
    val address: String,
    val neighborhood: String,
    val city: String,
    val phone: String,
    val lastSaleDate: String,
    val distanceKm: Double,
    val cnpj: String,
    val latitude: Double,
    val longitude: Double
)
