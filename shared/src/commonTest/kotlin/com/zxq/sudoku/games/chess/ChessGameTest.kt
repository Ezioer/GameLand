package com.zxq.sudoku.games.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChessGameTest {
    @Test
    fun startingBoardHasCorrectPiecesAndRedMovesFirst() {
        val game = newChessGame(ChessMode.Friend)
        assertEquals(32, game.board.count { it != null })
        assertEquals(ChessSide.Red, game.turn)
        assertEquals(ChessPieceType.General, game.board[9 * 9 + 4]?.type)
        assertFalse(ChessRules.isInCheck(game.board, ChessSide.Red))
        assertFalse(ChessRules.isInCheck(game.board, ChessSide.Black))
        assertTrue(ChessMove(6 * 9, 5 * 9) in game.legalMoves(6 * 9))
        assertNull(game.move(ChessMove(6 * 9, 4 * 9)))
    }

    @Test
    fun horseLegElephantEyeAndRiverAreEnforced() {
        val board = emptyBoard().apply {
            this[9 * 9 + 4] = red(ChessPieceType.General)
            this[4] = black(ChessPieceType.General)
            this[5 * 9 + 4] = red(ChessPieceType.Pawn)
            this[6 * 9 + 4] = red(ChessPieceType.Horse)
            this[7 * 9 + 2] = red(ChessPieceType.Elephant)
            this[8 * 9 + 3] = red(ChessPieceType.Pawn)
            this[6 * 9 + 3] = red(ChessPieceType.Pawn)
        }
        val horse = ChessRules.legalMoves(board, ChessSide.Red).filter { it.from == 6 * 9 + 4 }
        assertFalse(ChessMove(6 * 9 + 4, 4 * 9 + 3) in horse)
        val elephant = ChessRules.legalMoves(board, ChessSide.Red).filter { it.from == 7 * 9 + 2 }
        assertFalse(ChessMove(7 * 9 + 2, 9 * 9 + 4) in elephant)
        assertFalse(ChessMove(7 * 9 + 2, 5 * 9 + 4) in elephant)
        val acrossRiver = board.toMutableList().apply {
            this[7 * 9 + 2] = null
            this[5 * 9 + 2] = red(ChessPieceType.Elephant)
        }
        assertFalse(ChessMove(5 * 9 + 2, 3 * 9 + 4) in ChessRules.legalMoves(acrossRiver, ChessSide.Red))
    }

    @Test
    fun cannonNeedsExactlyOneScreenToCapture() {
        val board = emptyBoard().apply {
            this[9 * 9 + 4] = red(ChessPieceType.General)
            this[4] = black(ChessPieceType.General)
            this[5 * 9 + 4] = red(ChessPieceType.Pawn)
            this[7 * 9] = red(ChessPieceType.Cannon)
            this[5 * 9] = red(ChessPieceType.Pawn)
            this[3 * 9] = black(ChessPieceType.Rook)
        }
        assertTrue(ChessMove(7 * 9, 3 * 9) in ChessRules.legalMoves(board, ChessSide.Red))
        assertFalse(ChessMove(7 * 9, 5 * 9) in ChessRules.legalMoves(board, ChessSide.Red))
        board[5 * 9] = null
        assertFalse(ChessMove(7 * 9, 3 * 9) in ChessRules.legalMoves(board, ChessSide.Red))
    }

    @Test
    fun generalsCannotFaceAndUndoRestoresTurn() {
        val board = emptyBoard().apply {
            this[9 * 9 + 4] = red(ChessPieceType.General)
            this[4] = black(ChessPieceType.General)
            this[5 * 9 + 4] = red(ChessPieceType.Rook)
        }
        assertFalse(ChessMove(5 * 9 + 4, 5 * 9 + 3) in ChessRules.legalMoves(board, ChessSide.Red))
        val game = newChessGame(ChessMode.Friend)
        val moved = assertNotNull(game.move(ChessMove(6 * 9, 5 * 9)))
        assertEquals(ChessSide.Black, moved.turn)
        assertEquals(game, moved.undo())
    }

    @Test
    fun humanUndoRewindsBothSidesAndAiChoosesLegalMove() {
        val start = newChessGame(ChessMode.Human, ChessLevel.Easy)
        val afterRed = assertNotNull(start.move(ChessMove(6 * 9, 5 * 9)))
        val reply = assertNotNull(ChessAi.choose(afterRed.board, ChessSide.Black, ChessLevel.Easy))
        assertTrue(reply in ChessRules.legalMoves(afterRed.board, ChessSide.Black))
        val afterBlack = assertNotNull(afterRed.move(reply))
        assertEquals(start, afterBlack.undo())
    }

    @Test
    fun puzzlesHaveBothGeneralsAndPlayableRedPosition() {
        ChessPuzzles.entries.forEach { puzzle ->
            assertEquals(1, puzzle.board.count { it?.side == ChessSide.Red && it.type == ChessPieceType.General })
            assertEquals(1, puzzle.board.count { it?.side == ChessSide.Black && it.type == ChessPieceType.General })
            assertTrue(ChessRules.legalMoves(puzzle.board, ChessSide.Red).isNotEmpty())
        }
    }

    @Test
    fun agreedDrawEndsGameAndPreventsFurtherMoves() {
        val game = newChessGame(ChessMode.Friend).agreeDraw()
        assertTrue(game.finished)
        assertTrue(game.drawn)
        assertNull(game.winner)
        assertNull(game.move(ChessMove(6 * 9, 5 * 9)))
    }

    private fun emptyBoard() = MutableList<ChessPiece?>(90) { null }
    private fun red(type: ChessPieceType) = ChessPiece(ChessSide.Red, type)
    private fun black(type: ChessPieceType) = ChessPiece(ChessSide.Black, type)
}
