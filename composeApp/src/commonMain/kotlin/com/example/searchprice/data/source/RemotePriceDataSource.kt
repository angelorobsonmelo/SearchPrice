package com.example.searchprice.data.source

import com.example.searchprice.BuildConfig
import com.example.searchprice.data.model.PriceSearchRequest
import com.example.searchprice.data.model.PriceSearchResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.json.Json

class RemotePriceDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = createHttpClient()

    suspend fun searchPrices(
        description: String,
        latitude: Double,
        longitude: Double
    ): Result<List<PriceSearchResponse>> = try {
        val request = PriceSearchRequest(
            descricao = description,
            latitude = latitude,
            longitude = longitude
        )
        val jsonBody = json.encodeToString(PriceSearchRequest.serializer(), request)
        val httpResponse = client.post(BASE_URL) {
            header("Authorization", "Token ${BuildConfig.APP_TOKEN}")
            setBody(TextContent(jsonBody, ContentType.Application.Json))
        }
        val body = httpResponse.bodyAsText()
        if (body.trimStart().startsWith("[")) {
            Result.success(json.decodeFromString<List<PriceSearchResponse>>(body))
        } else {
            Result.failure(Exception("A API retornou um erro. Tente refinar sua busca."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    companion object {
        private const val BASE_URL =
            "https://api.sefaz.al.gov.br/sfz-economiza-alagoas-api/api/public/produto/pesquisar"
    }
}
