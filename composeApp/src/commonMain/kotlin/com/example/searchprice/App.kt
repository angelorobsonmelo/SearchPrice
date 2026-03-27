package com.example.searchprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.searchprice.di.appModule
import com.example.searchprice.presentation.ui.SearchPriceScreen
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            SearchPriceScreen()
        }
    }
}
