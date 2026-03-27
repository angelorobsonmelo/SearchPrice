package com.example.searchprice.auto

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.SessionInfo
import androidx.car.app.validation.HostValidator

/**
 * Entry point for the Android Auto integration.
 *
 * Declared in AndroidManifest.xml with:
 *   action  = androidx.car.app.CarAppService
 *   category = androidx.car.app.category.POI
 *
 * The POI category grants access to [androidx.car.app.model.PlaceListMapTemplate]
 * and [androidx.car.app.model.SearchTemplate], which are the two templates this
 * app uses. It intentionally does NOT use the NAVIGATION category because the app
 * delegates turn-by-turn directions to the head unit's installed navigation app.
 *
 * [HostValidator.ALLOW_ALL_HOSTS_VALIDATOR] is used here for development / open-source
 * distribution. Replace with a certificate-based allowlist before publishing on the
 * Google Play Store (see Car App Library docs on host validation).
 */
class SearchPriceCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator =
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(sessionInfo: SessionInfo): Session =
        SearchPriceSession()
}
