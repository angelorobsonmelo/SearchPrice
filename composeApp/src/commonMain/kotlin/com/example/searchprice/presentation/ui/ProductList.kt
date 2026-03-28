package com.example.searchprice.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.searchprice.presentation.contract.SearchContract
import org.jetbrains.compose.resources.stringResource
import searchprice.composeapp.generated.resources.Res
import searchprice.composeapp.generated.resources.empty_results_message
import searchprice.composeapp.generated.resources.empty_results_title
import searchprice.composeapp.generated.resources.error_retry_fallback
import searchprice.composeapp.generated.resources.error_search_message
import searchprice.composeapp.generated.resources.error_title
import searchprice.composeapp.generated.resources.initial_hint
import searchprice.composeapp.generated.resources.initial_title
import searchprice.composeapp.generated.resources.loading_message
import searchprice.composeapp.generated.resources.retry_button

// ── Spacing ───────────────────────────────────────────────────────────────────
private val SpacingSmall   = 8.dp
private val SpacingMedium  = 12.dp
private val SpacingLarge   = 16.dp
private val SpacingXLarge  = 24.dp
private val SpacingXXLarge = 32.dp

// ── Shape ─────────────────────────────────────────────────────────────────────
private val CornerRadiusCard = 16.dp

// ── List / Grid padding ───────────────────────────────────────────────────────
private val ListContentPadding = PaddingValues(
    horizontal = SpacingLarge,
    vertical   = SpacingSmall
)

// ── Typography ────────────────────────────────────────────────────────────────
private val FontSizeBody  = 14.sp
private val FontSizeTitle = 18.sp
private val FontSizeError = 20.sp

@Composable
fun ProductList(
    state: SearchContract.State,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> LoadingContent()
        state.error != null -> ErrorContent(message = state.error, onRetry = onRetry)
        state.hasSearched && state.products.isEmpty() -> EmptyResultsContent()
        !state.hasSearched -> InitialContent()
        else -> ResultsList(state = state)
    }
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PriceGreen)
            Spacer(Modifier.height(SpacingLarge))
            Text(
                stringResource(Res.string.loading_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(SpacingXXLarge),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            shape = RoundedCornerShape(CornerRadiusCard)
        ) {
            Column(
                modifier = Modifier.padding(SpacingXLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(Res.string.error_title), fontSize = FontSizeError, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(SpacingSmall))
                Text(
                    stringResource(
                        Res.string.error_search_message,
                        message.ifBlank { stringResource(Res.string.error_retry_fallback) }
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = FontSizeBody
                )
                Spacer(Modifier.height(SpacingLarge))
                FilledTonalButton(onClick = onRetry) {
                    Text(stringResource(Res.string.retry_button))
                }
            }
        }
    }
}

@Composable
private fun EmptyResultsContent() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(SpacingXXLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(Res.string.empty_results_title), fontSize = FontSizeTitle, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SpacingSmall))
            Text(
                stringResource(Res.string.empty_results_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = FontSizeBody
            )
        }
    }
}

@Composable
private fun InitialContent() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(SpacingXXLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(Res.string.initial_title),
                fontSize = FontSizeTitle,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(SpacingSmall))
            Text(
                stringResource(Res.string.initial_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = FontSizeBody
            )
        }
    }
}

@Composable
private fun ResultsList(state: SearchContract.State) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowSize = WindowSize.from(maxWidth)
        val columns = windowSize.gridColumns

        if (columns == 1) {
            LazyColumn(
                contentPadding = ListContentPadding,
                verticalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                items(state.products) { product ->
                    ProductItemCard(product = product)
                }
                item { Spacer(Modifier.height(SpacingMedium)) }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = ListContentPadding,
                verticalArrangement = Arrangement.spacedBy(SpacingMedium),
                horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                items(state.products) { product ->
                    ProductItemCard(product = product)
                }
            }
        }
    }
}
