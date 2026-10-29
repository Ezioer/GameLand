package com.zxq.sudoku.games.sokoban

import kotlin.test.Test
import kotlin.test.assertEquals

class SokobanStatisticsTest {
    @Test
    fun completedRoundCountsOnceEvenAfterUndo() {
        val start = SokobanGame.start(0)
        val first = start.move(SokobanDirection.Up)
        val once = SokobanStatistics().started(0).progressed(start, first, "2026-10-26")
        val undone = first.undo()
        val again = undone.move(SokobanDirection.Up)
        val twice = once.progressed(undone, again, "2026-10-26")
        assertEquals(1, twice.forLevel(0).played)
        assertEquals(1, twice.forLevel(0).completed)
        assertEquals(1, twice.forLevel(0).bestMoves)
    }

    @Test
    fun recordsRestoreAndInvalidDataFallsBack() {
        val stats = SokobanStatistics().started(0).started(1)
        assertEquals(stats, SokobanStatistics.restore(stats.serialize()))
        assertEquals(SokobanStatistics(), SokobanStatistics.restore("v1|broken"))
    }
}
