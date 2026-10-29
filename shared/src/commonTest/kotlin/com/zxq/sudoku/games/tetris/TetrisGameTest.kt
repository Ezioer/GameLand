package com.zxq.sudoku.games.tetris

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TetrisGameTest {
    private val first: (Int) -> Int = { 0 }

    @Test fun sevenBagAndMovement() {
        var game = TetrisGame.start(first)
        val seen = mutableListOf<Tetromino>()
        repeat(7) {
            seen += game.active.kind
            game = game.copy(board = List(TetrisCells) { null }, active = FallingPiece(game.active.kind))
                .hardDrop(first)
        }
        assertEquals(Tetromino.entries.filter { it != Tetromino.G }.toSet(), seen.toSet())
        assertEquals(7, seen.size)
        assertEquals(7, game.pieces)
        val atWall = TetrisGame.start(first).copy(active = FallingPiece(Tetromino.I, x = 0, y = 0))
        assertEquals(atWall, atWall.move(-1))
        assertEquals(4, atWall.rotate().active.cells.size)
    }

    @Test fun lineClearScoresAndRaisesLevel() {
        val board = MutableList<Tetromino?>(TetrisCells) { null }
        for (col in 0 until TetrisWidth) if (col !in 3..6)
            board[(TetrisHeight - 1) * TetrisWidth + col] = Tetromino.O
        val game = TetrisGame(board, FallingPiece(Tetromino.I, x = 3, y = 18),
            listOf(Tetromino.O, Tetromino.T, Tetromino.L), emptyList(), lines = 9)
        val next = game.hardDrop(first)
        assertEquals(10, next.lines)
        assertEquals(2, next.level)
        assertEquals(100, next.score)
        assertEquals(1, next.pieces)
        assertTrue(next.board.all { it == null })
    }

    @Test fun holdOnceUntilLockAndPause() {
        val game = TetrisGame.start(first)
        val held = game.hold(first)
        assertEquals(game.active.kind, held.held)
        assertEquals(game.next.first(), held.active.kind)
        assertFalse(held.canHold)
        assertEquals(held, held.hold(first))
        val paused = held.pause()
        assertEquals(paused, paused.tick(first))
        assertEquals(paused, paused.move(1))
        val locked = paused.resume().hardDrop(first)
        assertTrue(locked.canHold)
    }

    @Test fun topOutAndSaveRestore() {
        val blocked = MutableList<Tetromino?>(TetrisCells) { null }.apply {
            this[0 * TetrisWidth + 4] = Tetromino.Z
        }
        val game = TetrisGame(blocked, FallingPiece(Tetromino.O),
            listOf(Tetromino.T, Tetromino.I, Tetromino.J), emptyList())
        assertTrue(game.tick(first).finished)
        val running = TetrisGame.start(first).softDrop(first).pause()
        assertEquals(running, assertNotNull(restoreTetrisGame(running.serialize())))
        assertEquals(null, restoreTetrisGame("bad data"))
    }

    @Test fun garbageRaisesBoardAndActivePiece() {
        val board = MutableList<Tetromino?>(TetrisCells) { null }.apply {
            this[(TetrisHeight - 1) * TetrisWidth] = Tetromino.T
        }
        val game = TetrisGame(board, FallingPiece(Tetromino.I, y = 10),
            listOf(Tetromino.O, Tetromino.T, Tetromino.L), emptyList())
        val raised = game.addGarbage(2) { 4 }

        assertEquals(8, raised.active.y)
        assertEquals(game.lines, raised.lines)
        assertEquals(TetrisCells, raised.board.size)
        assertEquals(Tetromino.T, raised.board[(TetrisHeight - 3) * TetrisWidth])
        for (row in TetrisHeight - 2 until TetrisHeight) {
            assertEquals(null, raised.board[row * TetrisWidth + 4])
            assertEquals(Tetromino.G, raised.board[row * TetrisWidth + 3])
        }
        assertFalse(raised.finished)
        assertEquals(raised, restoreTetrisGame(raised.serialize()))
    }

    @Test fun garbagePushingBlocksPastTopEndsGame() {
        val board = MutableList<Tetromino?>(TetrisCells) { null }.apply { this[0] = Tetromino.Z }
        val game = TetrisGame(board, FallingPiece(Tetromino.O, y = 5),
            listOf(Tetromino.T, Tetromino.I, Tetromino.J), emptyList())
        assertTrue(game.addGarbage(1, first).finished)
    }

    @Test fun statisticsOnlyCountsFinishedOnce() {
        val start = TetrisGame.start(first)
        val stats = TetrisStatistics().started()
        val progressed = start.copy(score = 120, lines = 2)
        val updated = stats.progressed(start, progressed, "today")
        assertEquals(120, updated.bestScore)
        assertEquals(2, updated.totalLines)
        val finished = updated.progressed(progressed, progressed.copy(finished = true), "later")
        assertEquals(1, finished.finished)
        assertEquals(finished, TetrisStatistics.restore(finished.serialize()))
    }
}
