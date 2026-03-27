package com.example.searchprice.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun RequestLocationEffect(onLocationReceived: (Double, Double) -> Unit) {
    LaunchedEffect(Unit) {
        val manager = CLLocationManager()
        val status = manager.authorizationStatus
        if (status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
        ) {
            manager.location?.coordinate?.useContents {
                onLocationReceived(latitude, longitude)
            }
        } else {
            manager.requestWhenInUseAuthorization()
        }
    }
}
