package com.example.searchprice.location

import androidx.compose.runtime.Composable

@Composable
actual fun RequestLocationEffect(onLocationReceived: (Double, Double) -> Unit) {
    // Desktop: no GPS available, uses default Maceio coordinates
}
