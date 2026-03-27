package com.example.searchprice.location

import androidx.compose.runtime.Composable

@Composable
actual fun RequestLocationEffect(onLocationReceived: (Double, Double) -> Unit) {
    // JS: uses default Maceio coordinates
}
