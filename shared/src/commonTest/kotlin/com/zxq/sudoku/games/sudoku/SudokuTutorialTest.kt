package com.zxq.sudoku.games.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals

class SudokuTutorialTest {
    @Test
    fun eachAnimatedUnitContainsDigitsOneThroughNine() {
        TutorialUnit.entries.forEach { unit ->
            val cells = tutorialCells(unit)
            assertEquals(9, cells.distinct().size)
            assertEquals((1..9).toList(), cells.map(::tutorialValue))
        }
    }
}
