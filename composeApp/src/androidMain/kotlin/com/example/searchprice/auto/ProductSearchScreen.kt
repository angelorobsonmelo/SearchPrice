package com.example.searchprice.auto

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarLocation
import androidx.car.app.model.Distance
import androidx.car.app.model.DistanceSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Metadata
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.PlaceMarker
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.searchprice.R
import com.example.searchprice.domain.model.Product
import com.example.searchprice.domain.usecase.SearchProductsUseCase
import com.example.searchprice.domain.util.SortOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Results screen for Android Auto.
 *
 * Reuses [SearchProductsUseCase] — the same domain use case the phone app's
 * [com.example.searchprice.presentation.viewmodel.SearchViewModel] uses — so
 * business logic and sorting rules are never duplicated.
 *
 * ## Template strategy
 *
 * | State          | Template                                    |
 * |----------------|---------------------------------------------|
 * | Loading        | [PlaceListMapTemplate] with setLoading(true) |
 * | Results        | [PlaceListMapTemplate] with product rows     |
 * | Empty results  | [MessageTemplate]                            |
 * | Error          | [MessageTemplate] with Retry action          |
 *
 * Using [PlaceListMapTemplate] for both the loading and results states avoids a
 * template-type change which would consume an extra step of the Car App Library's
 * 5-step quota per task.
 *
 * ## Sort filters
 *
 * An [ActionStrip] with two toggle actions mirrors the phone app's [FilterChips].
 * The active sort is visually indicated by a "✓" prefix in the action title.
 * Re-sorting re-uses the cached [allProducts] list without a new network request,
 * matching [SearchViewModel]'s behaviour exactly.
 *
 * ## Navigation
 *
 * Tapping a product row triggers [CarContext.ACTION_NAVIGATE] with a `geo:` URI.
 * This delegates turn-by-turn directions to the head unit's installed navigation
 * app (Google Maps, Waze, etc.) rather than rendering a custom map, which is the
 * only permitted approach for a `POI`-category Car App.
 *
 * ## Item limit
 *
 * The row limit is read at runtime from [ConstraintManager.CONTENT_LIMIT_TYPE_PLACE_LIST]
 * so the head unit decides how many items to show. Results are sorted before truncation.
 */
class ProductSearchScreen(
    carContext: CarContext,
    private val query: String,
    private val userLat: Double,
    private val userLon: Double,
) : Screen(carContext) {

    private val searchUseCase: SearchProductsUseCase = GlobalContext.get().get()

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    /** Limit reported by the head unit — may exceed 6 on some hosts. */
    private val maxItems: Int by lazy {
        carContext.getCarService(ConstraintManager::class.java)
            .getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_PLACE_LIST)
    }

    // ── State ─────────────────────────────────────────────────────────────────

    /** Sorted slice shown to the driver (capped by [maxItems]). */
    private var products: List<Product> = emptyList()

    /** Full unsorted result list; kept so re-sort never triggers a new request. */
    private var allProducts: List<Product> = emptyList()

    private var isLoading = true
    private var errorMessage: String? = null
    private var sortOption = SortOption.PRICE

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
        fetchProducts()
    }

    // ── Template building ─────────────────────────────────────────────────────

    override fun onGetTemplate(): Template {
        // Terminal states use MessageTemplate for clarity.
        errorMessage?.let { return buildErrorTemplate(it) }
        if (!isLoading && products.isEmpty()) return buildEmptyTemplate()

        // Loading and results both stay on PlaceListMapTemplate to avoid
        // consuming an extra quota step on state transition.
        return buildPlaceListTemplate()
    }

    private fun buildPlaceListTemplate(): Template {
        val title = if (isLoading) {
            carContext.getString(R.string.auto_searching, query)
        } else {
            val shown = minOf(products.size, maxItems)
            if (products.size > maxItems)
                carContext.getString(R.string.auto_results_truncated, query, shown, products.size)
            else
                carContext.getString(R.string.auto_results_title, query, products.size)
        }
        val builder = PlaceListMapTemplate.Builder()
            .setTitle(title)
            .setHeaderAction(Action.BACK)
            .setLoading(isLoading)

        if (!isLoading) {
            builder
                .setItemList(buildItemList())
                .setActionStrip(buildSortActionStrip())
        }

        return builder.build()
    }

    private fun buildItemList(): ItemList {
        val listBuilder = ItemList.Builder()
        products.take(maxItems).forEach { product ->
            listBuilder.addItem(buildProductRow(product))
        }
        return listBuilder.build()
    }

    private fun buildProductRow(product: Product): Row {
        val storeName = product.storeFantasyName ?: product.storeName
        val priceText = "R\$ ${product.price.formatAuto(2)}"

        // PlaceListMapTemplate requires a DistanceSpan on every non-browsable row.
        val prefix = "$priceText  ·  "
        val title = SpannableString("$prefix ")
        title.setSpan(
            DistanceSpan.create(Distance.create(product.distanceKm, Distance.UNIT_KILOMETERS)),
            prefix.length,
            title.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        return Row.Builder()
            // Title: price + distance (DistanceSpan lets the head unit format the distance).
            .setTitle(title)
            // Line 1: product name.
            .addText(product.name)
            // Line 2: store name + neighbourhood.
            .addText("$storeName  ·  ${product.neighborhood}")
            // Place metadata pins the product on the map.
            .setMetadata(
                Metadata.Builder()
                    .setPlace(
                        Place.Builder(CarLocation.create(product.latitude, product.longitude))
                            .setMarker(
                                PlaceMarker.Builder()
                                    .setColor(CarColor.GREEN)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            // Tapping a row opens the store location in the head unit's nav app.
            .setOnClickListener { navigateTo(product) }
            .build()
    }

    private fun buildSortActionStrip(): ActionStrip {
        val priceActive = sortOption == SortOption.PRICE
        val distActive = sortOption == SortOption.DISTANCE
        val sortPrice = carContext.getString(R.string.auto_sort_price)
        val sortDist = carContext.getString(R.string.auto_sort_distance)
        return ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle(if (priceActive) "✓ $sortPrice" else sortPrice)
                    .setOnClickListener { applySort(SortOption.PRICE) }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle(if (distActive) "✓ $sortDist" else sortDist)
                    .setOnClickListener { applySort(SortOption.DISTANCE) }
                    .build()
            )
            .build()
    }

    private fun buildEmptyTemplate(): Template =
        MessageTemplate.Builder(carContext.getString(R.string.auto_empty_message, query))
            .setTitle(carContext.getString(R.string.auto_empty_title))
            .setHeaderAction(Action.BACK)
            .build()

    private fun buildErrorTemplate(message: String): Template =
        MessageTemplate.Builder(message)
            .setTitle(carContext.getString(R.string.auto_error_title))
            .setHeaderAction(Action.BACK)
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.auto_retry))
                    .setOnClickListener { retrySearch() }
                    .build()
            )
            .build()

    // ── Business logic ────────────────────────────────────────────────────────

    private fun fetchProducts() {
        isLoading = true
        errorMessage = null
        invalidate()

        scope.launch {
            searchUseCase(query, userLat, userLon, sortOption)
                .onSuccess { result ->
                    allProducts = result
                    products = sort(result, sortOption)
                    isLoading = false
                    errorMessage = null
                }
                .onFailure { e ->
                    allProducts = emptyList()
                    products = emptyList()
                    isLoading = false
                    errorMessage = e.message ?: carContext.getString(R.string.auto_error_fallback)
                }
            invalidate()
        }
    }

    private fun retrySearch() {
        errorMessage = null
        fetchProducts()
    }

    /**
     * Re-sorts the cached list without a new network request — identical to
     * [SearchViewModel.changeSortOption].
     */
    private fun applySort(option: SortOption) {
        if (sortOption == option) return
        sortOption = option
        products = sort(allProducts, option)
        invalidate()
    }

    private fun sort(items: List<Product>, option: SortOption): List<Product> = when (option) {
        SortOption.PRICE -> items.sortedBy { it.price }
        SortOption.DISTANCE -> items.sortedBy { it.distanceKm }
    }

    /**
     * Delegates navigation to the head unit's installed nav app via a `geo:` URI.
     * [CarContext.ACTION_NAVIGATE] is the correct intent action for POI-category
     * apps — the Car App Library routes it to whichever navigation app is default
     * on the head unit (Google Maps, Waze, etc.).
     */
    private fun navigateTo(product: Product) {
        val storeName = Uri.encode(product.storeFantasyName ?: product.storeName)
        val geoUri = "geo:${product.latitude},${product.longitude}" +
                "?q=${product.latitude},${product.longitude}($storeName)"
        carContext.startCarApp(
            Intent(CarContext.ACTION_NAVIGATE, Uri.parse(geoUri))
        )
    }

}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Local copy of [com.example.searchprice.presentation.ui.formatDecimals] for
 * use in Car App screens (which are Android-only, not Compose).
 * Duplicated here intentionally — Car App screens have no Composable context and
 * share no UI layer with the phone app.
 */
private fun Double.formatAuto(decimals: Int): String {
    val factor = if (decimals == 0) 1.0 else ("1" + "0".repeat(decimals)).toDouble()
    val rounded = kotlin.math.round(this * factor) / factor
    val parts = rounded.toString().split('.')
    val intPart = parts[0]
    val fracPart = (parts.getOrElse(1) { "" }).padEnd(decimals, '0').take(decimals)
    return if (decimals > 0) "$intPart,$fracPart" else intPart
}
