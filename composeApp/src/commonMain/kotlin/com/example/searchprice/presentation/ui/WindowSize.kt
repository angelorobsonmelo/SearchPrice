package com.example.searchprice.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSize {
    Compact,  // < 600dp  — phones
    Medium,   // 600–899dp — tablets, large phones, narrow desktop
    Expanded; // ≥ 900dp  — desktop, wide web

    companion object {
        fun from(width: Dp): WindowSize = when {
            width >= 900.dp -> Expanded
            width >= 600.dp -> Medium
            else            -> Compact
        }
    }
}

val WindowSize.gridColumns: Int get() = when (this) {
    WindowSize.Compact  -> 1
    WindowSize.Medium   -> 2
    WindowSize.Expanded -> 3
}
