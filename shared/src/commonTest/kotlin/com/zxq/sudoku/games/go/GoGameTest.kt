package com.zxq.sudoku.games.go

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoGameTest {
    private fun point(row: Int, column: Int) = row * GoSize + column

    @Test
    fun blackStartsAndCannotUseOccupiedPoint() {
        val start = GoGame(GoMode.Friend)
        val next = assertNotNull(start.place(point(4, 4)))
        assertEquals(GoStone.White, next.turn)
        assertNull(next.place(point(4, 4)))
        assertNull(start.place(-1))
        assertNull(start.place(GoCells))
    }

    @Test
    fun capturesGroupWithNoLiberties() {
        val board = MutableList<GoStone?>(GoCells) { null }
        board[point(4, 4)] = GoStone.White
        listOf(point(3, 4), point(5, 4), point(4, 3)).forEach { board[it] = GoStone.Black }
        val next = assertNotNull(GoGame(GoMode.Friend, board = board).place(point(4, 5)))
        assertNull(next.board[point(4, 4)])
        assertEquals(1, next.blackCaptures)
    }

    @Test
    fun connectedStonesAreCapturedTogether() {
        val board = MutableList<GoStone?>(GoCells) { null }
        board[point(4, 4)] = GoStone.White
        board[point(4, 5)] = GoStone.White
        listOf(point(3, 4), point(3, 5), point(5, 4), point(5, 5), point(4, 3))
            .forEach { board[it] = GoStone.Black }
        val next = assertNotNull(GoGame(GoMode.Friend, board = board).place(point(4, 6)))
        assertNull(next.board[point(4, 4)])
        assertNull(next.board[point(4, 5)])
        assertEquals(2, next.blackCaptures)
    }

    @Test
    fun suicideIsIllegalUnlessItCaptures() {
        val board = MutableList<GoStone?>(GoCells) { null }
        listOf(point(3, 4), point(5, 4), point(4, 3), point(4, 5))
            .forEach { board[it] = GoStone.White }
        assertNull(GoGame(GoMode.Friend, board = board).place(point(4, 4)))
        val captureBoard = board.toMutableList()
        listOf(point(2, 4), point(3, 3), point(3, 5), point(6, 4),
            point(5, 3), point(5, 5), point(4, 2), point(4, 6))
            .forEach { captureBoard[it] = GoStone.Black }
        val next = assertNotNull(GoGame(GoMode.Friend, board = captureBoard).place(point(4, 4)))
        assertEquals(4, next.blackCaptures)
    }

    @Test
    fun immediateKoRecaptureIsForbidden() {
        val board = MutableList<GoStone?>(GoCells) { null }
        listOf(point(0, 1), point(1, 0), point(2, 1)).forEach { board[it] = GoStone.Black }
        listOf(point(0, 2), point(1, 1), point(1, 3), point(2, 2)).forEach { board[it] = GoStone.White }
        val captured = assertNotNull(GoGame(GoMode.Friend, board = board).place(point(1, 2)))
        assertNull(captured.board[point(1, 1)])
        assertNull(captured.place(point(1, 1)))
        val afterThreat = assertNotNull(captured.place(point(8, 8)))
        val answered = assertNotNull(afterThreat.place(point(8, 7)))
        assertNotNull(answered.place(point(1, 1)))
    }

    @Test
    fun twoPassesStartScoringAndDeadGroupCanBeMarked() {
        val board = MutableList<GoStone?>(GoCells) { null }
        board[point(4, 4)] = GoStone.White
        listOf(point(3, 4), point(5, 4), point(4, 3), point(4, 5))
            .forEach { board[it] = GoStone.Black }
        val scoring = GoGame(GoMode.Friend, board = board).pass().pass()
        assertEquals(GoPhase.Scoring, scoring.phase)
        assertNull(scoring.place(point(0, 0)))
        val marked = scoring.toggleDead(point(4, 4))
        assertNull(marked.effectiveBoard[point(4, 4)])
        assertEquals(GoPhase.Finished, marked.finishScoring().phase)
        assertEquals(GoStone.Black, marked.finishScoring().winner)
        assertEquals(GoPhase.Playing, scoring.resumePlaying().phase)
    }

    @Test
    fun markingOneStoneMarksItsConnectedDeadGroup() {
        val board = MutableList<GoStone?>(GoCells) { null }
        board[point(4, 4)] = GoStone.White
        board[point(4, 5)] = GoStone.White
        val scoring = GoGame(GoMode.Friend, board = board).pass().pass()
        val marked = scoring.toggleDead(point(4, 4))
        assertEquals(setOf(point(4, 4), point(4, 5)), marked.deadStones)
        assertTrue(marked.toggleDead(point(4, 5)).deadStones.isEmpty())
    }

    @Test
    fun areaScoringIncludesStonesAndSingleColorTerritory() {
        val board = MutableList<GoStone?>(GoCells) { GoStone.White }
        for (row in 3..5) for (column in 3..5) board[point(row, column)] = GoStone.Black
        board[point(4, 4)] = null
        val score = GoRules.score(board)
        assertEquals(9, score.blackArea)
        assertEquals(72, score.whiteArea)
        assertEquals(GoStone.White, score.winner)
    }

    @Test
    fun undoAndFinishedGame() {
        val start = GoGame(GoMode.Human, GoLevel.Easy)
        val afterBlack = assertNotNull(start.place(point(4, 4)))
        assertEquals(start, afterBlack.undo())
        val afterWhite = assertNotNull(afterBlack.place(point(4, 5)))
        assertEquals(start, afterWhite.undo())
        val resigned = start.resign()
        assertTrue(resigned.finished)
        assertEquals(GoStone.White, resigned.winner)
        assertNull(resigned.place(point(4, 4)))
        val drawn = start.agreeDraw()
        assertTrue(drawn.drawn)
        assertFalse(start.finished)
    }

    @Test
    fun aiChoosesLegalMoveAtEveryDifficulty() {
        val game = assertNotNull(GoGame(GoMode.Human).place(point(4, 4)))
        for (level in GoLevel.entries) {
            val choice = assertNotNull(GoAi.choose(game.copy(level = level)))
            assertNotNull(game.place(choice))
        }
    }
}
