package com.example.searchprice.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.searchprice.domain.model.Product
import org.jetbrains.compose.resources.stringResource
import searchprice.composeapp.generated.resources.Res
import searchprice.composeapp.generated.resources.last_sale_date
import searchprice.composeapp.generated.resources.open_in_maps

@Composable
fun ProductItemCard(product: Product) {
    val uriHandler = LocalUriHandler.current
    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${product.latitude},${product.longitude}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "R$ ${product.price.formatDecimals(2)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = PriceGreen
                    )
                    if (product.minPrice != product.maxPrice) {
                        Text(
                            text = "Min R$ ${product.minPrice.formatDecimals(2)} - Max R$ ${product.maxPrice.formatDecimals(2)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentOrange.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${product.distanceKm.formatDecimals(1)} km",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentOrange
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.storeFantasyName ?: product.storeName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { uriHandler.openUri(mapsUrl) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(Res.string.open_in_maps),
                        tint = AccentOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "${product.address} - ${product.neighborhood}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.city,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (product.lastSaleDate.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.last_sale_date, product.lastSaleDate),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

internal fun Double.formatDecimals(decimals: Int): String {
    val factor = if (decimals == 0) 1.0 else ("1" + "0".repeat(decimals)).toDouble()
    val rounded = kotlin.math.round(this * factor) / factor
    val parts = rounded.toString().split('.')
    val intPart = parts[0]
    val fracPart = (parts.getOrElse(1) { "" }).padEnd(decimals, '0').take(decimals)
    return if (decimals > 0) "$intPart.$fracPart" else intPart
}
