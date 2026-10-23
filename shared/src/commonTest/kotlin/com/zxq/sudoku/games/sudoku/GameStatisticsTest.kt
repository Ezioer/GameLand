package com.zxq.sudoku.games.sudoku

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class GameStatisticsTest {
    @Test
    fun startsAndTerminalResultsAreCountedOncePerDifficulty() {
        val beginner = SudokuPuzzle.create(SudokuDifficulty.Beginner, Random(1))
        val emptyCell = beginner.givens.indexOf(0)
        val answer = beginner.solution[emptyCell]
        val wrong = if (answer == 9) 1 else answer + 1
        val started = SudokuGame.start(beginner).select(emptyCell)
        val failed = started.enter(wrong).enter(wrong).enter(wrong)

        var statistics = GameStatistics().started(SudokuDifficulty.Beginner)
        statistics = statistics.afterGameChange(started, started)
        statistics = statistics.afterGameChange(started, failed)
        statistics = statistics.afterGameChange(failed, failed)
        assertEquals(DifficultyStatistics(1, 0, 1), statistics.forDifficulty(SudokuDifficulty.Beginner))

        val almostSolved = beginner.solution.toMutableList().also { it[emptyCell] = 0 }
        val finalPuzzle = SudokuPuzzle(almostSolved, beginner.solution, SudokuDifficulty.Beginner)
        val finalStart = SudokuGame.start(finalPuzzle).select(emptyCell)
        val won = finalStart.enter(answer)
        statistics = statistics.started(SudokuDifficulty.Beginner).afterGameChange(finalStart, won)
        statistics = statistics.started(SudokuDifficulty.Easy)
        assertEquals(DifficultyStatistics(2, 1, 1), statistics.forDifficulty(SudokuDifficulty.Beginner))
        assertEquals(DifficultyStatistics(1, 0, 0), statistics.forDifficulty(SudokuDifficulty.Easy))
        assertEquals(statistics, GameStatistics.restore(statistics.serialize()))
    }

    @Test
    fun invalidStoredDataFallsBackToEmptyStatistics() {
        assertEquals(GameStatistics(), GameStatistics.restore("broken"))
    }

    @Test
    fun eachStartedGameKeepsItsDateTimeMistakesAndResult() {
        val puzzle = SudokuPuzzle.create(SudokuDifficulty.Easy, Random(2))
        val cell = puzzle.givens.indexOf(0)
        val answer = puzzle.solution[cell]
        val wrong = if (answer == 9) 1 else answer + 1
        val first = SudokuGame.start(puzzle).select(cell)
        val failed = first.enter(wrong).enter(wrong).enter(wrong).tick()

        var statistics = GameStatistics().started(SudokuDifficulty.Easy, "2026-10-15")
        statistics = statistics.afterGameChange(first, failed)
        statistics = statistics.started(SudokuDifficulty.Easy, "2026-10-16")
        val second = SudokuGame.start(puzzle).tick().tick().select(cell).enter(wrong)
        statistics = statistics.afterGameChange(SudokuGame.start(puzzle), second).left(second)

        assertEquals(
            listOf(
                GameRecord("2026-10-15", 0, 3, GameResult.Lost),
                GameRecord("2026-10-16", 2, 1, GameResult.Left),
            ),
            statistics.gamesFor(SudokuDifficulty.Easy),
        )
        assertEquals(statistics, GameStatistics.restore(statistics.serialize()))
    }

    @Test
    fun oldSummaryStillRestoresWithoutGameHistory() {
        val oldFormat = SudokuDifficulty.entries.joinToString(";") { "1,0,0" }
        val restored = GameStatistics.restore(oldFormat)
        assertEquals(1, restored.forDifficulty(SudokuDifficulty.Beginner).played)
        assertEquals(emptyList(), restored.gamesFor(SudokuDifficulty.Beginner))
    }
}
