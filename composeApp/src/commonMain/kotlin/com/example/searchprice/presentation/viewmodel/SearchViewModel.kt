package com.example.searchprice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.searchprice.domain.model.Product
import com.example.searchprice.domain.usecase.GetLocationUseCase
import com.example.searchprice.domain.usecase.SearchProductsUseCase
import com.example.searchprice.domain.util.SortOption
import com.example.searchprice.presentation.contract.SearchContract
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchContract.State())
    val state: StateFlow<SearchContract.State> = _state.asStateFlow()

    private val _effects = Channel<SearchContract.Effect>(Channel.BUFFERED)
    val effects: Flow<SearchContract.Effect>     = _effects.receiveAsFlow()

    // Cached unsorted results for re-sorting without re-fetching
    private var allProducts: List<Product> = emptyList()
    private var userLatitude: Double
    private var userLongitude: Double

    init {
        val (lat, lon) = getLocationUseCase.getDefaultLocation()
        userLatitude = lat
        userLongitude = lon
    }

    fun handleIntent(intent: SearchContract.Intent) {
        when (intent) {
            is SearchContract.Intent.UpdateQuery ->
                _state.update { it.copy(query = intent.query) }
            is SearchContract.Intent.UpdateLocation ->
                updateLocation(intent.latitude, intent.longitude)
            is SearchContract.Intent.ChangeSortOption ->
                changeSortOption(intent.option)
            SearchContract.Intent.PerformSearch,
            SearchContract.Intent.RetrySearch ->
                performSearch()
        }
    }

    private fun updateLocation(latitude: Double, longitude: Double) {
        userLatitude = latitude
        userLongitude = longitude
    }

    private fun changeSortOption(option: SortOption) {
        val sorted = sort(allProducts, option)
        _state.update { it.copy(sortOption = option, products = sorted) }
    }

    private fun performSearch() {
        val query = _state.value.query.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = true,
                error = null,
                hasSearched = true
            ) }
            searchProductsUseCase(query, userLatitude, userLongitude, _state.value.sortOption)
                .onSuccess { products ->
                    allProducts = products
                    _state.update { it.copy(isLoading = false, products = products) }
                }
                .onFailure { e ->
                    allProducts = emptyList()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            products = emptyList(),
                            error = e.message ?: ""
                        )
                    }
                }
        }
    }

    private fun sort(items: List<Product>, option: SortOption): List<Product> = when (option) {
        SortOption.PRICE -> items.sortedBy { it.price }
        SortOption.DISTANCE -> items.sortedBy { it.distanceKm }
    }
}
