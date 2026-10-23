package com.zxq.sudoku.games.sudoku

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SudokuGameTest {
    @Test
    fun everyDifficultyProducesAValidUniquePuzzle() {
        SudokuDifficulty.entries.forEach { difficulty ->
            val puzzle = SudokuPuzzle.create(difficulty, Random(42))
            assertEquals(difficulty.clues, puzzle.givens.count { it != 0 })
            assertTrue(puzzle.givens.indices.all { puzzle.givens[it] == 0 || puzzle.givens[it] == puzzle.solution[it] })
            for (index in 0..8) {
                assertEquals((1..9).toSet(), (0..8).map { puzzle.solution[index * 9 + it] }.toSet())
                assertEquals((1..9).toSet(), (0..8).map { puzzle.solution[it * 9 + index] }.toSet())
                val boxRow = index / 3 * 3
                val boxColumn = index % 3 * 3
                assertEquals(
                    (1..9).toSet(),
                    (0..8).map { puzzle.solution[(boxRow + it / 3) * 9 + boxColumn + it % 3] }.toSet(),
                )
            }
            assertEquals(1, countSolutions(puzzle.givens.toMutableList()))
        }
    }

    @Test
    fun notesMovesMistakesAndVictoryFollowTheRules() {
        val generated = SudokuPuzzle.create(SudokuDifficulty.Beginner, Random(1))
        val cell = generated.givens.indexOf(0)
        val answer = generated.solution[cell]
        val wrong = if (answer == 9) 1 else answer + 1
        var game = SudokuGame.start(generated).select(cell)
        assertEquals(9 - generated.givens.count { it == answer }, game.remaining(answer))
        game = game.toggleNotes().enter(answer)
        assertEquals(0, game.values[cell])
        assertTrue(game.notes[cell] and (1 shl answer) != 0)
        assertEquals(game, SudokuGame.restore(game.serialize()))
        game = game.undo()
        assertEquals(0, game.notes[cell])
        game = game.toggleNotes().enter(wrong)
        assertEquals(1, game.mistakes)
        assertEquals(0, game.values[cell])
        game = game.enter(answer)
        assertEquals(answer, game.values[cell])
        assertEquals(9 - game.values.count { it == answer }, game.remaining(answer))
        game = game.undo()
        assertEquals(0, game.values[cell])
        assertEquals(1, game.mistakes)
        game = game.enter(wrong).enter(wrong)
        assertEquals(3, game.mistakes)
        assertTrue(game.finished)
        assertEquals(0, game.enter(answer).values[cell])

        val almostDone = generated.solution.toMutableList().also { it[cell] = 0 }
        val finalPuzzle = SudokuPuzzle(almostDone, generated.solution, SudokuDifficulty.Beginner)
        val finishedGame = SudokuGame.start(finalPuzzle).select(cell).enter(answer)
        assertTrue(finishedGame.won)
        assertFalse(finishedGame.canUndo)
    }

    @Test
    fun revealOneFillsSelectedCellWithoutMistakeAndCanBeUndone() {
        val puzzle = SudokuPuzzle.create(SudokuDifficulty.Beginner, Random(7))
        val cell = puzzle.givens.indexOfLast { it == 0 }
        val game = SudokuGame.start(puzzle).select(cell).toggleNotes()
        val revealed = game.revealOne()

        assertEquals(puzzle.solution[cell], revealed.values[cell])
        assertEquals(0, revealed.mistakes)
        assertTrue(revealed.noteMode)
        assertEquals(revealed, SudokuGame.restore(revealed.serialize()))
        assertEquals(0, revealed.undo().values[cell])
        assertEquals(game, game.togglePause().revealOne().togglePause())
        val firstEmpty = puzzle.givens.indexOf(0)
        assertEquals(puzzle.solution[firstEmpty], SudokuGame.start(puzzle).revealOne().values[firstEmpty])
    }

    @Test
    fun completionAnimationTargetsOnlyNewlyCompletedUnits() {
        val solution = SudokuPuzzle.create(SudokuDifficulty.Beginner, Random(5)).solution
        val rowAlmostDone = List(81) { cell -> if (cell < 8) solution[cell] else 0 }
        val rowFinished = rowAlmostDone.toMutableList().also { it[8] = solution[8] }
        assertEquals((0..8).toSet(), newlyCompletedCells(rowAlmostDone, rowFinished))
        assertTrue(newlyCompletedCells(rowFinished, rowFinished).isEmpty())

        val oneMissing = solution.toMutableList().also { it[0] = 0 }
        val expected = (0..8).toSet() + (0..8).map { it * 9 } +
            (0..2).flatMap { row -> (0..2).map { column -> row * 9 + column } }
        assertEquals(expected, newlyCompletedCells(oneMissing, solution))

        val repeatedDigit = rowAlmostDone.toMutableList().also { it[8] = solution[0] }
        assertTrue(newlyCompletedCells(rowAlmostDone, repeatedDigit).isEmpty())
    }

    @Test
    fun completionWaveSpreadsFromEnteredCell() {
        val solution = SudokuPuzzle.create(SudokuDifficulty.Beginner, Random(5)).solution
        val oneMissing = solution.toMutableList().also { it[40] = 0 }
        val units = newlyCompletedUnits(oneMissing, solution)
        assertEquals(3, units.size)
        val row = units.single { it.type == CompletedUnitType.Row }
        val column = units.single { it.type == CompletedUnitType.Column }
        val box = units.single { it.type == CompletedUnitType.Box }

        assertTrue(completionWaveAmount(row, 40, 40, 0.3f) > completionWaveAmount(row, 40, 41, 0.3f))
        assertEquals(completionWaveAmount(row, 40, 39, 0.3f), completionWaveAmount(row, 40, 41, 0.3f))
        assertEquals(0f, completionWaveAmount(row, 40, 44, 0.3f))
        assertTrue(completionWaveAmount(column, 40, 31, 0.3f) > completionWaveAmount(column, 40, 4, 0.3f))
        assertTrue(completionWaveAmount(box, 40, 41, 0.6f) > completionWaveAmount(box, 40, 30, 0.6f))
        assertEquals(1f, completionWaveAmount(box, 40, 30, 1f))
    }

    @Test
    fun hintsProvideThreeStepsAndRespectDifficultyTiers() {
        SudokuDifficulty.entries.forEach { difficulty ->
            val game = SudokuGame.start(SudokuPuzzle.create(difficulty, Random(42)))
            val hint = assertNotNull(findSudokuHint(game))
            assertEquals(3, hint.steps.size)
            assertTrue(hint.steps.all { it.cells.isNotEmpty() })
            assertEquals(hint.isPlacement, puzzleOutcomeIsCorrect(game.puzzle.solution, hint))
            if (difficulty == SudokuDifficulty.Beginner || difficulty == SudokuDifficulty.Easy) {
                assertTrue(hint.technique == HintTechnique.NakedSingle || hint.technique == HintTechnique.HiddenSingle)
            }
            assertEquals(null, findSudokuHint(game.togglePause()))
        }
    }

    @Test
    fun advancedPatternsEliminateOnlyTheExpectedCandidate() {
        fun solver(vararg entries: Pair<Int, Set<Int>>): HintSolver {
            val candidates = MutableList(81) { emptySet<Int>() }
            entries.forEach { (cell, digits) -> candidates[cell] = digits }
            return HintSolver(List(81) { 0 }, candidates)
        }

        val locked = assertNotNull(solver(
            0 to setOf(5, 6), 1 to setOf(5, 7), 3 to setOf(5, 8),
        ).lockedCandidate())
        assertEquals(HintTechnique.LockedCandidate, locked.technique)
        assertEquals(3, locked.targetCell)
        assertEquals(5, locked.digit)

        val nakedPair = assertNotNull(solver(
            0 to setOf(2, 7), 1 to setOf(2, 7), 2 to setOf(2, 4),
        ).nakedPair())
        assertEquals(HintTechnique.NakedPair, nakedPair.technique)
        assertEquals(2, nakedPair.targetCell)
        assertEquals(2, nakedPair.digit)

        val hiddenPair = assertNotNull(solver(
            0 to setOf(2, 7, 8), 1 to setOf(2, 7, 9),
        ).hiddenPair())
        assertEquals(HintTechnique.HiddenPair, hiddenPair.technique)
        assertEquals(0, hiddenPair.targetCell)
        assertEquals(8, hiddenPair.digit)

        val xWing = assertNotNull(solver(
            0 to setOf(5), 3 to setOf(5), 9 to setOf(5), 12 to setOf(5),
            18 to setOf(5, 6),
        ).xWing())
        assertEquals(HintTechnique.XWing, xWing.technique)
        assertEquals(18, xWing.targetCell)
        assertEquals(5, xWing.digit)

        val xyWing = assertNotNull(solver(
            0 to setOf(1, 2), 1 to setOf(1, 3), 9 to setOf(2, 3),
            10 to setOf(3, 4),
        ).xyWing())
        assertEquals(HintTechnique.XYWing, xyWing.technique)
        assertEquals(10, xyWing.targetCell)
        assertEquals(3, xyWing.digit)
    }

    private fun puzzleOutcomeIsCorrect(solution: List<Int>, hint: SudokuHint): Boolean =
        solution[hint.targetCell] == hint.digit

    private fun countSolutions(board: MutableList<Int>, limit: Int = 2): Int {
        val empty = board.indices.filter { board[it] == 0 }
        if (empty.isEmpty()) return 1
        fun candidates(cell: Int): List<Int> {
            val row = cell / 9
            val column = cell % 9
            val used = (0..8).map { board[row * 9 + it] } +
                (0..8).map { board[it * 9 + column] } +
                (0..8).map { board[(row / 3 * 3 + it / 3) * 9 + column / 3 * 3 + it % 3] }
            return (1..9).filter { it !in used }
        }
        val cell = empty.minBy { candidates(it).size }
        var count = 0
        for (candidate in candidates(cell)) {
            board[cell] = candidate
            count += countSolutions(board, limit)
            board[cell] = 0
            if (count >= limit) break
        }
        return count
    }
}
