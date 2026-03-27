package com.example.searchprice.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import searchprice.composeapp.generated.resources.Res
import searchprice.composeapp.generated.resources.app_name
import searchprice.composeapp.generated.resources.app_subtitle
import com.example.searchprice.location.RequestLocationEffect
import com.example.searchprice.presentation.contract.SearchContract
import com.example.searchprice.presentation.viewmodel.SearchViewModel

// Shared color tokens for the presentation layer
internal val PriceGreen = Color(0xFF2E7D32)
internal val PriceGreenLight = Color(0xFFE8F5E9)
internal val AccentOrange = Color(0xFFFF8F00)
internal val CardBackground = Color(0xFFFAFAFA)
private val HeaderGradientStart = Color(0xFF1B5E20)
private val HeaderGradientEnd = Color(0xFF43A047)

@Composable
fun SearchPriceScreen(
    viewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    RequestLocationEffect { lat, lon ->
        viewModel.handleIntent(SearchContract.Intent.UpdateLocation(lat, lon))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchContract.Effect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)
                SearchContract.Effect.NavigateBack -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { SearchPriceTopBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PriceSearchBar(
                query = state.query,
                isLoading = state.isLoading,
                onQueryChange = { viewModel.handleIntent(SearchContract.Intent.UpdateQuery(it)) },
                onSearch = { viewModel.handleIntent(SearchContract.Intent.PerformSearch) }
            )

            if (state.products.isNotEmpty()) {
                SortFilterChips(
                    selected = state.sortOption,
                    onSelect = { viewModel.handleIntent(SearchContract.Intent.ChangeSortOption(it)) },
                    resultCount = state.products.size
                )
            }

            ProductList(
                state = state,
                onRetry = { viewModel.handleIntent(SearchContract.Intent.RetrySearch) }
            )
        }
    }
}

@Composable
private fun SearchPriceTopBar() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(HeaderGradientStart, HeaderGradientEnd)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = stringResource(Res.string.app_name),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(Res.string.app_subtitle),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}
