package com.zxq.sudoku

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun launchSudokuWeb() {
    ComposeViewport(viewportContainerId = "webApp") {
        App()
    }
}
