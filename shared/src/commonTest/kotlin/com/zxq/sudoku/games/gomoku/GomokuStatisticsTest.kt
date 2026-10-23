package com.zxq.sudoku.games.gomoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GomokuStatisticsTest {
    @Test
    fun unfinishedGameDoesNotCreateRecord() {
        val game = assertNotNull(GomokuGame(GomokuMode.Friend).place(112))
        assertEquals(emptyList(), GomokuStatistics().completed(game, 1_000, "开始", 5_000, "离开").records)
    }

    @Test
    fun finishedGameKeepsBoardTimeAndRoundTrip() {
        val game = assertNotNull(GomokuGame(GomokuMode.Friend).place(112)).resign()
        val statistics = GomokuStatistics().completed(game, 1_000, "开始", 12_000, "结束")
        val record = statistics.records.single()
        assertEquals(GomokuResult.BlackWon, record.result)
        assertEquals(11, record.elapsedSeconds)
        assertEquals(1, record.blackMoves)
        assertEquals(0, record.whiteMoves)
        assertEquals(game.board, GomokuBoardCode.decode(record.finalBoard))
        assertEquals(statistics, GomokuStatistics.restore(statistics.serialize()))
    }

    @Test
    fun drawIsRecorded() {
        val statistics = GomokuStatistics().completed(GomokuGame(GomokuMode.Friend).agreeDraw(),
            1_000, "开始", 2_000, "结束")
        assertEquals(GomokuResult.Draw, statistics.records.single().result)
    }
}
