package com.zxq.sudoku.games.minesweeper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MinesweeperGameTest {
    @Test
    fun firstRevealKeepsItsNeighborhoodSafe() {
        MinesweeperDifficulty.entries.forEach { difficulty ->
            val start = MinesweeperGame.start(difficulty)
            val first = start.cellCount / 2
            val opened = start.reveal(first) { 0 }
            assertEquals(difficulty.mineCount, opened.mines.size)
            assertTrue(first in opened.revealed)
            assertTrue((start.neighbors(first) + first).none { it in opened.mines })
            assertEquals(start, start.tick())
            val running = start.copy(mines = (0 until difficulty.mineCount).toSet())
            assertEquals(1, running.tick().elapsedSeconds)
        }
    }

    @Test
    fun zeroRegionExpandsAndFlagsBlockOpening() {
        val board = MinesweeperGame.start(MinesweeperDifficulty.Easy)
            .copy(mines = (0 until 10).toSet())
        val expanded = board.reveal(80)
        assertTrue(expanded.revealed.size > 1)
        assertTrue(expanded.revealed.none { it in board.mines })
        val flagged = board.toggleFlag(80)
        assertEquals(flagged, flagged.reveal(80))
        assertFalse(80 in flagged.revealed)
        assertEquals(board, flagged.toggleFlag(80))
    }

    @Test
    fun chordOpensNeighborsAndWrongFlagCanLose() {
        val board = MinesweeperGame.start(MinesweeperDifficulty.Easy)
            .copy(mines = setOf(0) + (50..58).toSet(), revealed = setOf(10))
        val safe = board.copy(flags = setOf(0)).chord(10)
        assertTrue(11 in safe.revealed)
        assertEquals(MinesweeperStatus.Playing, board.copy(flags = setOf(1)).status)
        val lost = board.copy(flags = setOf(1)).chord(10)
        assertEquals(MinesweeperStatus.Lost, lost.status)
        assertEquals(0, lost.exploded)
        assertEquals(lost, lost.reveal(2))
    }

    @Test
    fun revealingAllSafeCellsWinsAndProgressRoundTrips() {
        val mines = (0 until 10).toSet()
        val almostWon = MinesweeperGame.start(MinesweeperDifficulty.Easy)
            .copy(mines = mines, revealed = (10 until 81).toSet() - 10)
        val won = almostWon.reveal(10)
        assertEquals(MinesweeperStatus.Won, won.status)
        assertEquals(won, assertNotNull(restoreMinesweeperGame(won.serialize())))
        assertEquals(won, won.tick())
        assertEquals(null, restoreMinesweeperGame("v1|broken"))
    }
}
