package com.zxq.sudoku.games.internationalchess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InternationalChessGameTest {
    @Test fun initialMovesAndCheckmate() {
        var game = ChessGame(ChessMode.Friend)
        assertEquals(32, game.position.board.count { it != null })
        assertEquals(20, game.legalMoves().size)
        assertNull(game.move(ChessMove(52, 28)))
        // Fool's mate: f3 e5 g4 Qh4#.
        for (move in listOf(ChessMove(53, 45), ChessMove(12, 28),
            ChessMove(54, 38), ChessMove(3, 39))) game = assertNotNull(game.move(move))
        assertEquals(ChessColor.Black, game.winner)
        assertTrue(game.finished)
        assertTrue(game.legalMoves().isEmpty())
    }

    @Test fun castlingAndCheckRestrictions() {
        val board = MutableList<Piece?>(64) { null }.apply {
            this[60] = Piece(ChessColor.White, PieceKind.King)
            this[63] = Piece(ChessColor.White, PieceKind.Rook)
            this[4] = Piece(ChessColor.Black, PieceKind.King)
        }
        val game = ChessGame(ChessMode.Friend, position = ChessPosition(board))
        val castle = ChessMove(60, 62)
        assertTrue(castle in game.legalMoves(60))
        val moved = assertNotNull(game.move(castle))
        assertEquals(Piece(ChessColor.White, PieceKind.Rook), moved.position.board[61])
        assertEquals(Piece(ChessColor.White, PieceKind.King), moved.position.board[62])
        assertFalse(moved.position.castling.whiteKing)
        val attacked = board.toMutableList().apply { this[5] = Piece(ChessColor.Black, PieceKind.Rook) }
        assertFalse(castle in ChessRules.legalMoves(ChessPosition(attacked)))
    }

    @Test fun enPassantOnlyOnFollowingMove() {
        val board = MutableList<Piece?>(64) { null }.apply {
            this[60] = Piece(ChessColor.White, PieceKind.King)
            this[4] = Piece(ChessColor.Black, PieceKind.King)
            this[28] = Piece(ChessColor.White, PieceKind.Pawn)
            this[11] = Piece(ChessColor.Black, PieceKind.Pawn)
        }
        val start = ChessGame(ChessMode.Friend, position = ChessPosition(board, ChessColor.Black))
        val pushed = assertNotNull(start.move(ChessMove(11, 27)))
        assertEquals(19, pushed.position.enPassant)
        val captured = assertNotNull(pushed.move(ChessMove(28, 19)))
        assertNull(captured.position.board[27])
        assertEquals(Piece(ChessColor.White, PieceKind.Pawn), captured.position.board[19])
        assertEquals(0, captured.position.halfmoves)
    }

    @Test fun pinnedPieceCannotExposeKing() {
        val board = MutableList<Piece?>(64) { null }.apply {
            this[60] = Piece(ChessColor.White, PieceKind.King)
            this[52] = Piece(ChessColor.White, PieceKind.Rook)
            this[4] = Piece(ChessColor.Black, PieceKind.Rook)
            this[0] = Piece(ChessColor.Black, PieceKind.King)
        }
        val game = ChessGame(ChessMode.Friend, position = ChessPosition(board))
        assertFalse(ChessMove(52, 53) in game.legalMoves(52))
        assertNull(game.move(ChessMove(52, 53)))
    }

    @Test fun promotionUndoAndStalemate() {
        val board = MutableList<Piece?>(64) { null }.apply {
            this[63] = Piece(ChessColor.White, PieceKind.King)
            this[7] = Piece(ChessColor.Black, PieceKind.King)
            this[8] = Piece(ChessColor.White, PieceKind.Pawn)
        }
        val game = ChessGame(ChessMode.Friend, position = ChessPosition(board))
        assertEquals(4, game.legalMoves(8).count { it.to == 0 })
        val promoted = assertNotNull(game.move(ChessMove(8, 0, PieceKind.Knight)))
        assertEquals(Piece(ChessColor.White, PieceKind.Knight), promoted.position.board[0])
        assertEquals(game.position, promoted.undo().position)

        val stalemate = MutableList<Piece?>(64) { null }.apply {
            this[0] = Piece(ChessColor.Black, PieceKind.King)
            this[18] = Piece(ChessColor.White, PieceKind.King)
            this[17] = Piece(ChessColor.White, PieceKind.Queen)
        }
        val position = ChessPosition(stalemate, ChessColor.Black)
        assertFalse(ChessRules.isInCheck(stalemate, ChessColor.Black))
        assertTrue(ChessRules.legalMoves(position).isEmpty())
    }

    @Test fun aiAndStatistics() {
        val game = ChessGame(ChessMode.Human, ChessLevel.Easy)
        val white = assertNotNull(game.move(ChessMove(52, 36)))
        val aiMove = assertNotNull(ChessAi.choose(white))
        assertTrue(aiMove in white.legalMoves())
        val black = assertNotNull(white.move(aiMove))
        assertEquals(game.position, black.undo().position)
        assertTrue(InternationalChessStatistics().completed(black, 1_000, "开始", 5_000, "结束").records.isEmpty())
        val finished = black.resign()
        val saved = InternationalChessStatistics().completed(finished, 1_000, "开始", 5_000, "结束")
        assertEquals(saved, InternationalChessStatistics.restore(saved.serialize()))
        assertEquals(1, saved.records.size)
        assertEquals(4, saved.records[0].elapsedSeconds)
    }
}
