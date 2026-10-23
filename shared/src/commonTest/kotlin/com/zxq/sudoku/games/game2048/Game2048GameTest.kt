package com.zxq.sudoku.games.game2048

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Game2048GameTest {
    private val firstEmpty: (Int) -> Int = { bound -> if (bound == 10) 1 else 0 }

    @Test
    fun newGameStartsWithTwoTiles() {
        val game = Game2048Game.start(firstEmpty)
        assertEquals(2, game.board.count { it != 0 })
        assertEquals(0, game.score)
        assertFalse(game.finished)
    }

    @Test
    fun eachTileMergesOnlyOncePerMove() {
        val game = Game2048Game(board = listOf(2, 2, 2, 2) + List(12) { 0 })
        val moved = game.slide(SlideDirection.Left, firstEmpty)
        assertEquals(listOf(4, 4, 2, 0), moved.board.take(4))
        assertEquals(8, moved.score)
        assertEquals(1, moved.moves)
    }

    @Test
    fun rightAndVerticalMovesUseCorrectOrder() {
        val row = Game2048Game(board = listOf(2, 2, 4, 4) + List(12) { 0 })
        val right = row.slide(SlideDirection.Right, firstEmpty)
        assertEquals(listOf(2, 0, 4, 8), right.board.take(4))
        assertEquals(12, right.score)
        val column = Game2048Game(board = listOf(2, 0, 0, 0, 2, 0, 0, 0) + List(8) { 0 })
        val down = column.slide(SlideDirection.Down, firstEmpty)
        assertEquals(4, down.board[12])
        assertEquals(4, down.score)
    }

    @Test
    fun blockedMoveDoesNotSpawnTileOrCreateHistory() {
        val game = Game2048Game(board = listOf(2, 4, 8, 16) + List(12) { 0 })
        assertEquals(game, game.slide(SlideDirection.Left, firstEmpty))
    }

    @Test
    fun fullBoardWithNoMergeIsFinished() {
        val board = List(Game2048Cells) { index ->
            if ((index / Game2048Size + index % Game2048Size) % 2 == 0) 2 else 4
        }
        val game = Game2048Game(board = board)
        assertTrue(game.finished)
        assertEquals(game, game.slide(SlideDirection.Up, firstEmpty))
    }

    @Test
    fun undoRestoresBoardAndScore() {
        val game = Game2048Game(board = listOf(2, 2) + List(14) { 0 })
        val moved = game.slide(SlideDirection.Left, firstEmpty)
        assertEquals(game, moved.undo())
    }

    @Test
    fun reaching2048CanContinueAfterWin() {
        val game = Game2048Game(board = listOf(1024, 1024) + List(14) { 0 })
        val won = game.slide(SlideDirection.Left, firstEmpty)
        assertTrue(won.won)
        assertEquals(2048, won.maxTile)
        assertEquals(2048, won.score)
        assertEquals(won, won.slide(SlideDirection.Down, firstEmpty))
        val continued = won.continuePlaying()
        assertFalse(continued.won)
        assertTrue(continued.slide(SlideDirection.Down, firstEmpty).moves > continued.moves)
    }

    @Test
    fun savedGameRestoresBoardScoreAndUndoHistory() {
        val start = Game2048Game(board = listOf(2, 2) + List(14) { 0 })
        val moved = start.slide(SlideDirection.Left, firstEmpty)
        val restored = restoreGame2048(moved.serialize())
        assertEquals(moved, restored)
        assertEquals(start, restored?.undo())
    }

    @Test
    fun savedWinCanStillContinueAfterRestore() {
        val won = Game2048Game(board = listOf(1024, 1024) + List(14) { 0 })
            .slide(SlideDirection.Left, firstEmpty)
        val restored = requireNotNull(restoreGame2048(won.serialize()))
        assertTrue(restored.won)
        assertTrue(restored.continuePlaying().everWon)
    }

    @Test
    fun invalidSaveIsIgnored() {
        assertEquals(null, restoreGame2048("v1|broken"))
        val invalidBoard = "v1|" + (listOf(3) + List(15) { 0 }).joinToString(",") + "|0|0|0|0|0|"
        assertEquals(null, restoreGame2048(invalidBoard))
    }
}
