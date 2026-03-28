package com.example.searchprice.data.source

import io.ktor.client.*
import io.ktor.client.plugins.*

internal fun HttpClientConfig<*>.installDefaults() {
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 30_000
        socketTimeoutMillis = 60_000
    }
}
