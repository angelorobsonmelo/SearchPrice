package com.example.searchprice

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val icon = Thread.currentThread()
        .contextClassLoader
        ?.getResourceAsStream("icons/SearchPrice.icns")
        ?.use { BitmapPainter(loadImageBitmap(it)) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "SearchPrice",
        icon = icon,
    ) {
        App()
    }
}