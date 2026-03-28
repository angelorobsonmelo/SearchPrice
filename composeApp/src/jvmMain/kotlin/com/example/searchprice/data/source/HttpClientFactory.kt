package com.example.searchprice.data.source

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            hostnameVerifier { hostname, _ -> hostname == "api.sefaz.al.gov.br" }
        }
    }
    installDefaults()
}
