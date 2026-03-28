package com.example.searchprice.presentation.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BREAKPOINT_MEDIUM   = 600.dp
private val BREAKPOINT_EXPANDED = 900.dp

private const val COLUMNS_COMPACT  = 1
private const val COLUMNS_MEDIUM   = 2
private const val COLUMNS_EXPANDED = 3

enum class WindowSize {
    Compact,  // < 600dp  — phones
    Medium,   // 600–899dp — tablets, large phones, narrow desktop
    Expanded; // ≥ 900dp  — desktop, wide web

    companion object {
        fun from(width: Dp): WindowSize = when {
            width >= BREAKPOINT_EXPANDED -> Expanded
            width >= BREAKPOINT_MEDIUM   -> Medium
            else                         -> Compact
        }
    }
}

val WindowSize.gridColumns: Int get() = when (this) {
    WindowSize.Compact  -> COLUMNS_COMPACT
    WindowSize.Medium   -> COLUMNS_MEDIUM
    WindowSize.Expanded -> COLUMNS_EXPANDED
}
