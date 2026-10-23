package com.zxq.sudoku

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "益智小岛",
    ) {
        App()
    }
}
