package com.zxq.sudoku.collection

import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionPlayStatisticsTest {
    @Test
    fun startsAndSecondsAreCountedByGame() {
        val statistics = CollectionPlayStatistics()
            .started(CollectionGame.Sudoku)
            .started(CollectionGame.Sudoku)
            .tick(CollectionGame.Sudoku)
            .started(CollectionGame.Minesweeper)
            .tick(CollectionGame.Minesweeper)
            .tick(CollectionGame.Minesweeper)

        assertEquals(GamePlayTotals(2, 1), statistics.forGame(CollectionGame.Sudoku))
        assertEquals(GamePlayTotals(1, 2), statistics.forGame(CollectionGame.Minesweeper))
        assertEquals(GamePlayTotals(), statistics.forGame(CollectionGame.Go))
        assertEquals(3L, statistics.totalRounds)
        assertEquals(3L, statistics.totalSeconds)
    }

    @Test
    fun totalsSurviveRestoreAndInvalidDataResets() {
        val statistics = CollectionPlayStatistics()
            .started(CollectionGame.ChineseChess)
            .tick(CollectionGame.ChineseChess)
        assertEquals(statistics, CollectionPlayStatistics.restore(statistics.serialize()))
        assertEquals(CollectionPlayStatistics(), CollectionPlayStatistics.restore("broken"))
    }

    @Test
    fun durationsUseReadableUnits() {
        assertEquals("0秒", formatPlayDuration(0))
        assertEquals("2分5秒", formatPlayDuration(125))
        assertEquals("1小时2分5秒", formatPlayDuration(3_725))
    }
}
