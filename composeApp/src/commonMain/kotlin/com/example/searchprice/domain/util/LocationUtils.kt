package com.example.searchprice.domain.util

import kotlin.math.*

const val DEFAULT_LATITUDE = -9.6483
const val DEFAULT_LONGITUDE = -35.7089

fun haversineDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadiusKm = 6371.0
    val dLat = toRadians(lat2 - lat1)
    val dLon = toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(toRadians(lat1)) * cos(toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

private fun toRadians(deg: Double): Double = deg * PI / 180.0
