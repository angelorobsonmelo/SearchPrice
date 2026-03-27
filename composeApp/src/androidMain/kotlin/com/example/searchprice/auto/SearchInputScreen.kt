package com.example.searchprice.auto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.searchprice.domain.usecase.GetLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.context.GlobalContext

/**
 * First screen shown when Android Auto connects.
 *
 * Presents a [SearchTemplate] (full-screen search bar with keyboard) so the
 * driver can enter a product name. On submission it resolves the user's current
 * location and immediately pushes [ProductSearchScreen], which owns the network
 * request and loading/results/error states.
 *
 * Coroutines are managed by [scope] which is cancelled when the screen is
 * destroyed to prevent leaks.
 */
class SearchInputScreen(carContext: CarContext) : Screen(carContext) {

    private val getLocationUseCase: GetLocationUseCase = GlobalContext.get().get()

    /** Cancelled in onDestroy so no coroutine outlives this screen. */
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val builder = SearchTemplate.Builder(searchCallback)
            .setHeaderAction(Action.APP_ICON)

        // setShowKeyboardByDefault requires Car App API Level 2.
        // Guard the call so Level-1 hosts don't throw IllegalStateException,
        // which would surface as "unexpected error" on the head unit.
        if (carContext.carAppApiLevel >= 2) {
            builder.setShowKeyboardByDefault(true)
        }

        return builder.build()
    }

    // ── SearchCallback ────────────────────────────────────────────────────────

    private val searchCallback = object : SearchTemplate.SearchCallback {
        override fun onSearchTextChanged(searchText: String) {
            // No incremental search — wait for explicit submission to keep
            // network usage low and avoid distracting the driver mid-typing.
        }

        override fun onSearchSubmitted(searchText: String) {
            val query = searchText.trim()
            if (query.isBlank()) return
            val (lat, lon) = resolveLocation()
            screenManager.push(ProductSearchScreen(carContext, query, lat, lon))
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns the best available location.
     *
     * Checks whether the ACCESS_COARSE_LOCATION permission is already granted
     * (it should be if the user previously opened the phone app). Falls back to
     * the default Maceió coordinates from [GetLocationUseCase] if permission is
     * absent or no cached fix is available — matching the behaviour of the phone
     * app's [com.example.searchprice.location.RequestLocationEffect].
     */
    private fun resolveLocation(): Pair<Double, Double> {
        val default = getLocationUseCase.getDefaultLocation()
        return try {
            val hasPermission = carContext.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return default

            @Suppress("MissingPermission")
            val lm = carContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val fix = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (fix != null) fix.latitude to fix.longitude else default
        } catch (_: Exception) {
            default
        }
    }
}
