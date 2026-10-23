package com.zxq.sudoku.games.sudoku

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SudokuBattleTest {
    @Test
    fun playersSharePuzzleButKeepIndependentMoves() {
        val puzzle = SudokuPuzzle.create(SudokuDifficulty.Beginner, Random(7))
        val cell = puzzle.givens.indexOf(0)
        val answer = puzzle.solution[cell]
        val wrong = if (answer == 9) 1 else answer + 1
        var battle = SudokuBattle.start(puzzle)

        battle = battle.updatePlayer(1) { it.select(cell).toggleNotes().enter(answer) }
        assertEquals(puzzle, battle.first.puzzle)
        assertEquals(puzzle, battle.second.puzzle)
        assertTrue(battle.first.notes[cell] != 0)
        assertEquals(0, battle.second.notes[cell])

        battle = battle.updatePlayer(1) { it.toggleNotes().enter(wrong).enter(wrong).enter(wrong) }
        assertTrue(battle.first.finished)
        assertFalse(battle.finished)
        battle = battle.updatePlayer(2) { it.select(cell).enter(answer) }
        assertEquals(answer, battle.second.values[cell])
        assertEquals(0, battle.first.values[cell])
        assertEquals(battle, SudokuBattle.restore(battle.serialize()))
    }

    @Test
    fun pauseAndVictoryStopTheMatch() {
        val puzzle = SudokuPuzzle.create(SudokuDifficulty.Easy, Random(8))
        val cell = puzzle.givens.indexOf(0)
        val givens = puzzle.solution.toMutableList().also { it[cell] = 0 }
        val almostSolved = SudokuPuzzle(givens, puzzle.solution, puzzle.difficulty)
        var battle = SudokuBattle.start(almostSolved).togglePause()
        assertEquals(battle, battle.tick())
        assertEquals(battle, battle.updatePlayer(1) { it.select(cell).enter(puzzle.solution[cell]) })

        battle = battle.togglePause().tick()
        assertEquals(1, battle.elapsedSeconds)
        battle = battle.updatePlayer(1) { it.select(cell).enter(puzzle.solution[cell]) }
        assertEquals(1, battle.winner)
        assertTrue(battle.finished)
        assertEquals(battle, battle.tick())
        assertEquals(battle, battle.updatePlayer(2) { it.select(cell).enter(puzzle.solution[cell]) })
    }
}
