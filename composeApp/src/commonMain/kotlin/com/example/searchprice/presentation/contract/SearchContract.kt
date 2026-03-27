package com.example.searchprice.presentation.contract

import com.example.searchprice.domain.model.Product
import com.example.searchprice.domain.util.SortOption

object SearchContract {

    data class State(
        val query: String = "",
        val isLoading: Boolean = false,
        val products: List<Product> = emptyList(),
        val sortOption: SortOption = SortOption.PRICE,
        val error: String? = null,
        val hasSearched: Boolean = false
    )

    sealed interface Intent {
        data class UpdateQuery(val query: String) : Intent
        data class UpdateLocation(val latitude: Double, val longitude: Double) : Intent
        data class ChangeSortOption(val option: SortOption) : Intent
        data object PerformSearch : Intent
        data object RetrySearch : Intent
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateBack : Effect
    }
}
