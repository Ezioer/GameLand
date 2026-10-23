package com.zxq.sudoku.games.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChessStatisticsTest {
    @Test
    fun boardSnapshotRoundTrips() {
        val board = ChessRules.initialBoard()
        assertEquals(board, ChessBoardCode.decode(ChessBoardCode.encode(board)))
    }

    @Test
    fun unfinishedGamesAreNotRecorded() {
        val start = newChessGame(ChessMode.Friend)
        val moved = assertNotNull(start.move(ChessMove(6 * 9, 5 * 9)))
        val statistics = ChessStatistics().completed(moved, 1_000, "开始", 5_000, "离开")
        assertEquals(emptyList(), statistics.records)
        assertEquals("v1|", statistics.serialize())
    }

    @Test
    fun resignationRecordsFinalBoardTimeAndMoves() {
        val start = newChessGame(ChessMode.Friend)
        val moved = assertNotNull(start.move(ChessMove(6 * 9, 5 * 9)))
        val statistics = ChessStatistics().completed(moved.resign(), 1_000, "开始", 11_000, "结束")
        val record = statistics.records.single()
        assertEquals(ChessResult.RedWon, record.result)
        assertEquals("结束", record.endedAt)
        assertEquals(10, record.elapsedSeconds)
        assertEquals(1, record.redMoves)
        assertEquals(0, record.blackMoves)
        assertEquals(moved.board, ChessBoardCode.decode(record.finalBoard))
        assertEquals(statistics, ChessStatistics.restore(statistics.serialize()))
    }

    @Test
    fun agreedDrawIsRecorded() {
        val game = newChessGame(ChessMode.Friend).agreeDraw()
        val statistics = ChessStatistics().completed(game, 1_000, "开始", 2_000, "结束")
        assertEquals(ChessResult.Draw, statistics.records.single().result)
        assertEquals(statistics, ChessStatistics.restore(statistics.serialize()))
    }

    @Test
    fun decisiveBoardResultIsRecorded() {
        val board = MutableList<ChessPiece?>(90) { null }.apply {
            this[9 * 9 + 4] = ChessPiece(ChessSide.Red, ChessPieceType.General)
            this[4] = ChessPiece(ChessSide.Black, ChessPieceType.General)
            this[9 + 4] = ChessPiece(ChessSide.Red, ChessPieceType.Rook)
        }
        val game = assertNotNull(ChessGame(board = board, mode = ChessMode.Friend)
            .move(ChessMove(9 + 4, 4)))
        assertEquals(ChessSide.Red, game.winner)
        val statistics = ChessStatistics().completed(game, 1_000, "开始", 2_000, "结束")
        assertEquals(ChessResult.RedWon, statistics.records.single().result)
    }

    @Test
    fun oldUnfinishedRecordsAreFilteredWhenRestored() {
        val board = ChessBoardCode.encode(ChessRules.initialBoard())
        fun row(result: ChessResult) = "Friend,,,开始,,1000,0,0,0,${result.name},$board"
        val restored = ChessStatistics.restore("v1|${row(ChessResult.InProgress)};${row(ChessResult.Left)};${row(ChessResult.RedWon)}")
        assertEquals(listOf(ChessResult.RedWon), restored.records.map(ChessRecord::result))
    }
}
