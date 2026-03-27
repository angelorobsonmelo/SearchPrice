package com.example.searchprice.location

import androidx.compose.runtime.Composable

@Composable
expect fun RequestLocationEffect(onLocationReceived: (Double, Double) -> Unit)
