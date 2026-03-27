package com.example.searchprice.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PriceSearchResponse(
    val codGetin: String? = null,
    val codNcm: String? = null,
    val dscProduto: String? = null,
    val valMinimoVendido: Double? = null,
    val valMaximoVendido: Double? = null,
    val dthEmissaoUltimaVenda: String? = null,
    val valUnitarioUltimaVenda: Double? = null,
    val valUltimaVenda: Double? = null,
    val numCNPJ: String? = null,
    val nomRazaoSocial: String? = null,
    val nomFantasia: String? = null,
    val numTelefone: String? = null,
    val nomLogradouro: String? = null,
    val numImovel: String? = null,
    val nomBairro: String? = null,
    val numCep: String? = null,
    val nomMunicipio: String? = null,
    val numLatitude: Double? = null,
    val numLongitude: Double? = null
)
