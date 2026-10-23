package com.zxq.sudoku.games.go

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoStatisticsTest {
    @Test
    fun onlyFinishedGamesAreRecorded() {
        val statistics = GoStatistics()
        assertEquals(statistics, statistics.completed(GoGame(GoMode.Friend), 0, "start", 5000, "end"))
        val recorded = statistics.completed(GoGame(GoMode.Friend).resign(), 0, "start", 5000, "end")
        assertEquals(1, recorded.records.size)
        assertEquals(GoResult.WhiteWon, recorded.records.single().result)
        assertEquals(5, recorded.records.single().elapsedSeconds)
    }

    @Test
    fun countResultAndBoardSurviveSerialization() {
        val game = GoGame(GoMode.Human, GoLevel.Hard).pass().pass().finishScoring()
        val statistics = GoStatistics().completed(game, 1000, "2026-10-23 10:00:00",
            3000, "2026-10-23 10:00:02")
        assertEquals(statistics, GoStatistics.restore(statistics.serialize()))
        assertEquals(GoEnding.Counted, statistics.records.single().ending)
        assertTrue(statistics.records.single().whiteArea != null)
    }

    @Test
    fun damagedSavedDataIsIgnored() {
        assertEquals(GoStatistics(), GoStatistics.restore("v1|broken"))
    }
}
