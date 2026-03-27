package com.example.searchprice.domain.usecase

import com.example.searchprice.domain.util.DEFAULT_LATITUDE
import com.example.searchprice.domain.util.DEFAULT_LONGITUDE

class GetLocationUseCase {
    fun getDefaultLocation(): Pair<Double, Double> = DEFAULT_LATITUDE to DEFAULT_LONGITUDE
}
