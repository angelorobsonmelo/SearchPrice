package com.example.searchprice.data.source

import io.ktor.client.*

actual fun createHttpClient(): HttpClient = HttpClient { installDefaults() }
