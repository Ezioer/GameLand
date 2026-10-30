package com.zxq.sudoku

fun main() = launchSudokuWeb(::openCyclingPage)

private fun openCyclingPage() {
    js("window.location.assign('/cycling/')")
}
