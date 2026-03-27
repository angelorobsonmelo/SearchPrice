package com.example.searchprice.data.mapper

import com.example.searchprice.data.model.PriceSearchResponse
import com.example.searchprice.domain.model.Product
import com.example.searchprice.domain.util.haversineDistance

fun PriceSearchResponse.toProduct(userLat: Double, userLon: Double): Product? {
    val price = valUltimaVenda ?: valUnitarioUltimaVenda ?: return null
    val lat = numLatitude ?: return null
    val lon = numLongitude ?: return null

    return Product(
        barcode = codGetin ?: "",
        name = dscProduto ?: "Produto sem nome",
        price = price,
        minPrice = valMinimoVendido ?: price,
        maxPrice = valMaximoVendido ?: price,
        storeName = nomRazaoSocial ?: "Loja desconhecida",
        storeFantasyName = nomFantasia,
        address = buildString {
            append(nomLogradouro ?: "")
            if (!numImovel.isNullOrBlank()) append(", $numImovel")
        },
        neighborhood = nomBairro ?: "",
        city = nomMunicipio ?: "",
        phone = numTelefone ?: "",
        lastSaleDate = dthEmissaoUltimaVenda?.formatDate() ?: "",
        distanceKm = haversineDistance(userLat, userLon, lat, lon),
        cnpj = numCNPJ ?: "",
        latitude = lat,
        longitude = lon
    )
}

private fun String.formatDate(): String {
    // "2026-02-07T00:56:07.000+0000" -> "07/02/2026"
    return try {
        val parts = substring(0, 10).split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (_: Exception) {
        this
    }
}
