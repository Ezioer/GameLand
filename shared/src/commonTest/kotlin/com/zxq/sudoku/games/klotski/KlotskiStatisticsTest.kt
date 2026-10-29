package com.zxq.sudoku.games.klotski

import kotlin.test.Test
import kotlin.test.assertEquals

class KlotskiStatisticsTest {
    @Test
    fun completingAfterUndoDoesNotCountTwice() {
        val start = KlotskiGame.start(0)
        val won = start.move(0, KlotskiDirection.Right)
        val once = KlotskiStatistics().started(0).progressed(start, won, "2026-10-26")
        val undone = won.undo()
        val again = undone.move(0, KlotskiDirection.Right)
        val twice = once.progressed(undone, again, "2026-10-26")
        assertEquals(1, twice.forLevel(0).played)
        assertEquals(1, twice.forLevel(0).completed)
        assertEquals(1, twice.forLevel(0).bestMoves)
    }

    @Test
    fun serializedRecordsRestore() {
        val stats = KlotskiStatistics().started(0).started(1)
        assertEquals(stats, KlotskiStatistics.restore(stats.serialize()))
        assertEquals(KlotskiStatistics(), KlotskiStatistics.restore("v1|broken"))
    }
}
