package com.example.searchprice.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PriceSearchRequest(
    val descricao: String,
    val dias: Int = 3,
    val latitude: Double = -9.6483,
    val longitude: Double = -35.7089,
    val raio: Int = 10
)
