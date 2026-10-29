package com.zxq.sudoku.games.minesweeper

import kotlin.test.Test
import kotlin.test.assertEquals

class MinesweeperStatisticsTest {
    @Test
    fun finishedGamesAreCountedOnceAndBestTimePersists() {
        val start = MinesweeperGame.start(MinesweeperDifficulty.Easy)
        val statistics = MinesweeperStatistics().started(MinesweeperDifficulty.Easy)
        val won = start.copy(status = MinesweeperStatus.Won, elapsedSeconds = 42)
        val afterWin = statistics.progressed(start, won, "2026-10-27 12:00")
        assertEquals(1, afterWin.forDifficulty(MinesweeperDifficulty.Easy).won)
        assertEquals(42, afterWin.forDifficulty(MinesweeperDifficulty.Easy).bestSeconds)
        assertEquals(afterWin, afterWin.progressed(won, won, "later"))

        val lost = start.copy(status = MinesweeperStatus.Lost, elapsedSeconds = 9)
        val afterLoss = afterWin.progressed(start, lost, "later")
        assertEquals(1, afterLoss.forDifficulty(MinesweeperDifficulty.Easy).lost)
        assertEquals(afterLoss, MinesweeperStatistics.restore(afterLoss.serialize()))
    }
}
