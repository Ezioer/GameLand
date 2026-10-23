package com.zxq.sudoku.games.gomoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GomokuGameTest {
    @Test
    fun blackStartsAndOccupiedPointCannotBePlayed() {
        val start = GomokuGame(GomokuMode.Friend)
        val afterBlack = assertNotNull(start.place(112))
        assertEquals(GomokuStone.White, afterBlack.turn)
        assertNull(afterBlack.place(112))
        assertNull(start.place(-1))
        assertNull(start.place(GomokuCells))
    }

    @Test
    fun fiveInEachDirectionWins() {
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
        for ((dr, dc) in directions) {
            val board = MutableList<GomokuStone?>(GomokuCells) { null }
            repeat(4) { step -> board[(5 + step * dr) * GomokuSize + 5 + step * dc] = GomokuStone.Black }
            val winningIndex = (5 + 4 * dr) * GomokuSize + 5 + 4 * dc
            val won = assertNotNull(GomokuGame(GomokuMode.Friend, board = board).place(winningIndex))
            assertEquals(GomokuStone.Black, won.winner)
            assertTrue(won.finished)
            assertNull(won.place(0))
        }
    }

    @Test
    fun fourInARowIsNotFinished() {
        val board = MutableList<GomokuStone?>(GomokuCells) { null }
        repeat(3) { board[7 * GomokuSize + it] = GomokuStone.Black }
        val game = assertNotNull(GomokuGame(GomokuMode.Friend, board = board).place(7 * GomokuSize + 3))
        assertFalse(game.finished)
    }

    @Test
    fun friendUndoOneMoveAndHumanUndoWholeRound() {
        val friend = assertNotNull(assertNotNull(GomokuGame(GomokuMode.Friend).place(112)).place(113))
        assertEquals(1, friend.undo().moves.size)
        val humanStart = GomokuGame(GomokuMode.Human, GomokuLevel.Easy)
        val afterHuman = assertNotNull(humanStart.place(112))
        assertEquals(humanStart, afterHuman.undo())
        val afterAi = assertNotNull(afterHuman.place(113))
        assertEquals(humanStart, afterAi.undo())
    }

    @Test
    fun resignationAndAgreedDrawEndTheGame() {
        val start = GomokuGame(GomokuMode.Friend)
        assertEquals(GomokuStone.White, start.resign().winner)
        val draw = start.agreeDraw()
        assertTrue(draw.finished)
        assertTrue(draw.drawn)
        assertNull(draw.winner)
        assertNull(draw.place(112))
    }

    @Test
    fun fullBoardWithoutFiveIsDraw() {
        val board = MutableList<GomokuStone?>(GomokuCells) { index ->
            val row = index / GomokuSize
            val column = index % GomokuSize
            if ((row + 2 * column) % 5 < 2) GomokuStone.Black else GomokuStone.White
        }
        board[GomokuCells - 1] = null
        val game = assertNotNull(GomokuGame(GomokuMode.Friend, board = board,
            turn = GomokuStone.White).place(GomokuCells - 1))
        assertTrue(game.drawn)
        assertNull(game.winner)
    }

    @Test
    fun mediumAndHardAiWinOrBlockImmediateFive() {
        val row = 7 * GomokuSize
        val winning = MutableList<GomokuStone?>(GomokuCells) { null }.apply {
            this[row + 2] = GomokuStone.Black
            repeat(4) { this[row + 3 + it] = GomokuStone.White }
        }
        val blocking = MutableList<GomokuStone?>(GomokuCells) { null }.apply {
            this[row + 2] = GomokuStone.White
            repeat(4) { this[row + 3 + it] = GomokuStone.Black }
        }
        for (level in listOf(GomokuLevel.Medium, GomokuLevel.Hard)) {
            assertEquals(row + 7, GomokuAi.choose(winning, GomokuStone.White, level))
            assertEquals(row + 7, GomokuAi.choose(blocking, GomokuStone.White, level))
        }
    }
}
