package com.example.searchprice.location

import androidx.compose.runtime.Composable

@Composable
actual fun RequestLocationEffect(onLocationReceived: (Double, Double) -> Unit) {
    // WasmJS: uses default Maceio coordinates
}
