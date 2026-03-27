package com.example.searchprice.presentation.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.searchprice.domain.util.SortOption
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import searchprice.composeapp.generated.resources.Res
import searchprice.composeapp.generated.resources.results_count
import searchprice.composeapp.generated.resources.sort_by_distance
import searchprice.composeapp.generated.resources.sort_by_price

@Composable
private fun SortOption.label(): String = when (this) {
    SortOption.PRICE -> stringResource(Res.string.sort_by_price)
    SortOption.DISTANCE -> stringResource(Res.string.sort_by_distance)
}

@Composable
fun SortFilterChips(
    selected: SortOption,
    onSelect: (SortOption) -> Unit,
    resultCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pluralStringResource(Res.plurals.results_count, resultCount, resultCount),
            fontSize = 13.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(12.dp))
        SortOption.entries.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.label(), fontSize = 13.sp) },
                modifier = Modifier.padding(end = 8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PriceGreenLight,
                    selectedLabelColor = PriceGreen
                )
            )
        }
    }
}
