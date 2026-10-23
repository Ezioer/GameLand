package com.zxq.sudoku.games.game2048

import kotlin.test.Test
import kotlin.test.assertEquals

class Game2048StatisticsTest {
    private val nextInt: (Int) -> Int = { bound -> if (bound == 10) 1 else 0 }

    @Test
    fun bestScoreSurvivesUndoAndRoundTrip() {
        val start = Game2048Game(board = listOf(2, 2) + List(14) { 0 })
        val moved = start.slide(SlideDirection.Left, nextInt)
        val statistics = Game2048Statistics().started().progressed(start, moved, "2026-10-23")
        assertEquals(4, statistics.bestScore)
        assertEquals(4, statistics.bestTile)
        assertEquals("2026-10-23", statistics.bestAt)
        assertEquals(statistics, statistics.progressed(moved, moved.undo(), "2026-10-24"))
        assertEquals(statistics, Game2048Statistics.restore(statistics.serialize()))
    }

    @Test
    fun winAndFinishedRoundAreCountedOnce() {
        val start = Game2048Game(board = listOf(1024, 1024) + List(14) { 0 })
        val won = start.slide(SlideDirection.Left, nextInt)
        val withWin = Game2048Statistics().started().progressed(start, won, "today")
        assertEquals(1, withWin.wins)
        assertEquals(1, withWin.progressed(won, won.continuePlaying(), "today").wins)
        val undone = won.undo()
        val wonAgain = undone.slide(SlideDirection.Left, nextInt)
        assertEquals(1, withWin.progressed(undone, wonAgain, "today").wins)
        val almostFull = Game2048Game(board = List(Game2048Cells) { index ->
            if (index == 0) 0 else index + 2
        })
        val full = almostFull.slide(SlideDirection.Left, nextInt)
        val ended = withWin.progressed(almostFull, full, "today")
        assertEquals(1, ended.completed)
        assertEquals(ended, ended.progressed(full, full, "today"))
    }

    @Test
    fun corruptStatisticsFallBackToEmpty() {
        assertEquals(Game2048Statistics(), Game2048Statistics.restore("v1|broken"))
    }
}
