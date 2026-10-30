package com.zxq.sudoku

fun main() = launchSudokuWeb(::openCyclingPage)

private fun openCyclingPage(): Unit = js("{ window.location.assign('/cycling/'); }")
