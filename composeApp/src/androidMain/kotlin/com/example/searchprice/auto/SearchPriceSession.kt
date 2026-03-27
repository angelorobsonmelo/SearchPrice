package com.example.searchprice.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

/**
 * Manages the Car App screen stack for one connection from a head unit.
 *
 * [onCreateScreen] is called once when Android Auto connects. It always starts
 * with [SearchInputScreen] so the driver is immediately prompted to enter a
 * product name — matching the search-first UX of the phone app.
 */
class SearchPriceSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen =
        SearchInputScreen(carContext)
}
