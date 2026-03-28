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
            Spacer(Modifier.height(16.dp))
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
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(Res.string.error_title), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(
                        Res.string.error_search_message,
                        message.ifBlank { stringResource(Res.string.error_retry_fallback) }
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
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
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(Res.string.empty_results_title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.empty_results_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun InitialContent() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(Res.string.initial_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.initial_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ResultsList(state: SearchContract.State) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 900.dp -> 3
            maxWidth >= 600.dp -> 2
            else               -> 1
        }
        val padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        val spacing = 12.dp

        if (columns == 1) {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                items(state.products) { product ->
                    ProductItemCard(product = product)
                }
                item { Spacer(Modifier.height(spacing)) }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                items(state.products) { product ->
                    ProductItemCard(product = product)
                }
            }
        }
    }
}
