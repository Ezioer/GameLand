package com.zxq.sudoku.games.sudoku

internal enum class TutorialUnit(val title: String, val rule: String) {
    Box("3 × 3 九宫格", "每个九宫格都要有 1 到 9，不能重复"),
    Row("一整行", "每一行都要有 1 到 9，不能重复"),
    Column("一整列", "每一列都要有 1 到 9，不能重复"),
}

internal fun tutorialValue(cell: Int): Int {
    val row = cell / 9
    val column = cell % 9
    return (row * 3 + row / 3 + column) % 9 + 1
}

internal fun tutorialCells(unit: TutorialUnit): List<Int> = when (unit) {
    TutorialUnit.Box -> (3..5).flatMap { row -> (3..5).map { column -> row * 9 + column } }
    TutorialUnit.Row -> (0..8).map { column -> 4 * 9 + column }
    TutorialUnit.Column -> (0..8).map { row -> row * 9 + 4 }
}.sortedBy(::tutorialValue)
