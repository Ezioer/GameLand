package com.zxq.sudoku

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun launchSudokuWeb(onCyclingClick: () -> Unit) {
    ComposeViewport(viewportContainerId = "webApp") {
        App(onCyclingWebClick = onCyclingClick)
    }
}
