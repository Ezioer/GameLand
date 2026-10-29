package com.zxq.sudoku.games.internationalchess

import kotlin.math.abs

internal enum class ChessColor(val label: String) {
    White("白方"), Black("黑方");
    fun other() = if (this == White) Black else White
}

internal enum class PieceKind(val symbol: String, val value: Int) {
    King("王", 0), Queen("后", 900), Rook("车", 500), Bishop("象", 330), Knight("马", 320), Pawn("兵", 100)
}

internal data class Piece(val color: ChessColor, val kind: PieceKind)
internal data class ChessMove(val from: Int, val to: Int, val promotion: PieceKind? = null)
internal enum class ChessMode { Human, Friend }
internal enum class ChessLevel(val label: String) { Easy("简单"), Medium("中级"), Hard("困难") }

internal data class CastlingRights(
    val whiteKing: Boolean = true, val whiteQueen: Boolean = true,
    val blackKing: Boolean = true, val blackQueen: Boolean = true,
) {
    fun can(color: ChessColor, kingSide: Boolean) = when {
        color == ChessColor.White && kingSide -> whiteKing
        color == ChessColor.White -> whiteQueen
        kingSide -> blackKing
        else -> blackQueen
    }
    fun revoke(color: ChessColor, kingSide: Boolean? = null) = when (color) {
        ChessColor.White -> copy(whiteKing = if (kingSide == false) whiteKing else false,
            whiteQueen = if (kingSide == true) whiteQueen else false)
        ChessColor.Black -> copy(blackKing = if (kingSide == false) blackKing else false,
            blackQueen = if (kingSide == true) blackQueen else false)
    }
}

internal data class ChessPosition(
    val board: List<Piece?>,
    val turn: ChessColor = ChessColor.White,
    val castling: CastlingRights = CastlingRights(),
    val enPassant: Int? = null,
    val halfmoves: Int = 0,
) {
    fun key(): String = buildString {
        board.forEach { piece -> append(if (piece == null) '.' else {
            val letter = when (piece.kind) {
                PieceKind.King -> 'k'; PieceKind.Queen -> 'q'; PieceKind.Rook -> 'r'
                PieceKind.Bishop -> 'b'; PieceKind.Knight -> 'n'; PieceKind.Pawn -> 'p'
            }
            if (piece.color == ChessColor.White) letter.uppercaseChar() else letter
        }) }
        append(if (turn == ChessColor.White) 'w' else 'b')
        append(if (castling.whiteKing) 'K' else '-')
        append(if (castling.whiteQueen) 'Q' else '-')
        append(if (castling.blackKing) 'k' else '-')
        append(if (castling.blackQueen) 'q' else '-')
        append(enPassant ?: -1)
    }
}

internal data class ChessGame(
    val mode: ChessMode,
    val level: ChessLevel? = null,
    val position: ChessPosition = ChessRules.initialPosition(),
    val history: List<ChessPosition> = emptyList(),
    val moves: List<ChessMove> = emptyList(),
    val positionKeys: List<String> = listOf(position.key()),
    val winner: ChessColor? = null,
    val drawReason: String? = null,
) {
    val finished get() = winner != null || drawReason != null
    val turn get() = position.turn
    fun legalMoves(from: Int? = null) = ChessRules.legalMoves(position).filter { from == null || it.from == from }

    fun move(move: ChessMove): ChessGame? {
        if (finished || move !in legalMoves(move.from)) return null
        val next = ChessRules.apply(position, move)
        val keys = positionKeys + next.key()
        val legal = ChessRules.legalMoves(next)
        val check = ChessRules.isInCheck(next.board, next.turn)
        val winner = if (legal.isEmpty() && check) turn else null
        val draw = when {
            legal.isEmpty() && !check -> "逼和"
            ChessRules.insufficientMaterial(next.board) -> "子力不足"
            next.halfmoves >= 150 -> "75 步规则"
            keys.count { it == next.key() } >= 5 -> "五次重复局面"
            else -> null
        }
        return copy(position = next, history = history + position, moves = moves + move,
            positionKeys = keys, winner = winner, drawReason = draw)
    }

    fun undo(): ChessGame {
        if (history.isEmpty()) return this
        val count = if (mode == ChessMode.Human && turn == ChessColor.White && history.size >= 2) 2 else 1
        return copy(position = history[history.size - count], history = history.dropLast(count),
            moves = moves.dropLast(count), positionKeys = positionKeys.dropLast(count),
            winner = null, drawReason = null)
    }

    fun resign() = if (finished) this else copy(winner = turn.other())
    fun agreeDraw() = if (finished) this else copy(drawReason = "双方同意")
}

internal object ChessRules {
    private val back = listOf(PieceKind.Rook, PieceKind.Knight, PieceKind.Bishop, PieceKind.Queen,
        PieceKind.King, PieceKind.Bishop, PieceKind.Knight, PieceKind.Rook)
    private val promotions = listOf(PieceKind.Queen, PieceKind.Rook, PieceKind.Bishop, PieceKind.Knight)
    private val knightSteps = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
    private val kingSteps = (-1..1).flatMap { dr -> (-1..1).map { dc -> dr to dc } }.filter { it != (0 to 0) }
    private val straight = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    private val diagonal = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)

    fun initialPosition(): ChessPosition = ChessPosition(List(64) { index ->
        val row = index / 8
        when (row) {
            0 -> Piece(ChessColor.Black, back[index % 8])
            1 -> Piece(ChessColor.Black, PieceKind.Pawn)
            6 -> Piece(ChessColor.White, PieceKind.Pawn)
            7 -> Piece(ChessColor.White, back[index % 8])
            else -> null
        }
    })

    fun legalMoves(position: ChessPosition): List<ChessMove> = position.board.indices.flatMap { from ->
        if (position.board[from]?.color != position.turn) emptyList()
        else pseudoMoves(position, from).filter { move ->
            !isInCheck(apply(position, move).board, position.turn)
        }
    }

    fun isInCheck(board: List<Piece?>, color: ChessColor): Boolean {
        val king = board.indexOfFirst { it == Piece(color, PieceKind.King) }
        return king < 0 || isAttacked(board, king, color.other())
    }

    fun isAttacked(board: List<Piece?>, square: Int, by: ChessColor): Boolean {
        val row = square / 8; val col = square % 8
        val pawnRow = row + if (by == ChessColor.White) 1 else -1
        for (dc in listOf(-1, 1)) if (onBoard(pawnRow, col + dc) &&
            board[pawnRow * 8 + col + dc] == Piece(by, PieceKind.Pawn)) return true
        for ((dr, dc) in knightSteps) if (onBoard(row + dr, col + dc) &&
            board[(row + dr) * 8 + col + dc] == Piece(by, PieceKind.Knight)) return true
        for ((dr, dc) in kingSteps) if (onBoard(row + dr, col + dc) &&
            board[(row + dr) * 8 + col + dc] == Piece(by, PieceKind.King)) return true
        for ((directions, kinds) in listOf(straight to setOf(PieceKind.Rook, PieceKind.Queen),
            diagonal to setOf(PieceKind.Bishop, PieceKind.Queen))) {
            for ((dr, dc) in directions) {
                var r = row + dr; var c = col + dc
                while (onBoard(r, c)) {
                    val piece = board[r * 8 + c]
                    if (piece != null) {
                        if (piece.color == by && piece.kind in kinds) return true
                        break
                    }
                    r += dr; c += dc
                }
            }
        }
        return false
    }

    private fun pseudoMoves(position: ChessPosition, from: Int): List<ChessMove> {
        val board = position.board; val piece = board[from] ?: return emptyList()
        val row = from / 8; val col = from % 8
        val result = mutableListOf<ChessMove>()
        fun add(to: Int) {
            if (board[to]?.kind == PieceKind.King) return
            if (piece.kind == PieceKind.Pawn && to / 8 == (if (piece.color == ChessColor.White) 0 else 7))
                promotions.forEach { result += ChessMove(from, to, it) }
            else result += ChessMove(from, to)
        }
        when (piece.kind) {
            PieceKind.Pawn -> {
                val direction = if (piece.color == ChessColor.White) -1 else 1
                val next = row + direction
                if (next in 0..7) {
                    val forward = next * 8 + col
                    if (board[forward] == null) {
                        add(forward)
                        val start = if (piece.color == ChessColor.White) 6 else 1
                        if (row == start && board[(row + 2 * direction) * 8 + col] == null)
                            add((row + 2 * direction) * 8 + col)
                    }
                    for (dc in listOf(-1, 1)) if (col + dc in 0..7) {
                        val target = next * 8 + col + dc
                        if (board[target]?.color == piece.color.other() || target == position.enPassant) add(target)
                    }
                }
            }
            PieceKind.Knight, PieceKind.King -> {
                val steps = if (piece.kind == PieceKind.Knight) knightSteps else kingSteps
                for ((dr, dc) in steps) if (onBoard(row + dr, col + dc)) {
                    val target = (row + dr) * 8 + col + dc
                    if (board[target]?.color != piece.color) add(target)
                }
                if (piece.kind == PieceKind.King && !isInCheck(board, piece.color)) {
                    val home = if (piece.color == ChessColor.White) 7 else 0
                    if (from == home * 8 + 4) for (kingSide in listOf(true, false)) {
                        val rookCol = if (kingSide) 7 else 0
                        val between = if (kingSide) listOf(5, 6) else listOf(1, 2, 3)
                        val crossing = if (kingSide) listOf(5, 6) else listOf(3, 2)
                        if (position.castling.can(piece.color, kingSide) &&
                            board[home * 8 + rookCol] == Piece(piece.color, PieceKind.Rook) &&
                            between.all { board[home * 8 + it] == null } &&
                            crossing.none { isAttacked(board, home * 8 + it, piece.color.other()) })
                            add(home * 8 + (if (kingSide) 6 else 2))
                    }
                }
            }
            else -> {
                val directions = when (piece.kind) {
                    PieceKind.Rook -> straight
                    PieceKind.Bishop -> diagonal
                    else -> straight + diagonal
                }
                for ((dr, dc) in directions) {
                    var r = row + dr; var c = col + dc
                    while (onBoard(r, c)) {
                        val target = r * 8 + c
                        if (board[target]?.color == piece.color) break
                        add(target)
                        if (board[target] != null) break
                        r += dr; c += dc
                    }
                }
            }
        }
        return result
    }

    fun apply(position: ChessPosition, move: ChessMove): ChessPosition {
        val board = position.board.toMutableList()
        val piece = board[move.from] ?: return position
        val captured = board[move.to]
        board[move.from] = null
        board[move.to] = if (move.promotion == null) piece else piece.copy(kind = move.promotion)
        if (piece.kind == PieceKind.Pawn && move.to == position.enPassant && captured == null &&
            abs(move.to % 8 - move.from % 8) == 1) board[move.from / 8 * 8 + move.to % 8] = null
        if (piece.kind == PieceKind.King && abs(move.to % 8 - move.from % 8) == 2) {
            val row = move.from / 8
            val kingSide = move.to % 8 == 6
            val rookFrom = row * 8 + (if (kingSide) 7 else 0)
            val rookTo = row * 8 + (if (kingSide) 5 else 3)
            board[rookTo] = board[rookFrom]; board[rookFrom] = null
        }
        var rights = position.castling
        if (piece.kind == PieceKind.King) rights = rights.revoke(piece.color)
        if (piece.kind == PieceKind.Rook) rights = revokeRook(rights, move.from)
        if (captured?.kind == PieceKind.Rook) rights = revokeRook(rights, move.to)
        return ChessPosition(board, piece.color.other(), rights,
            enPassant = if (piece.kind == PieceKind.Pawn && abs(move.to - move.from) == 16)
                (move.from + move.to) / 2 else null,
            halfmoves = if (piece.kind == PieceKind.Pawn || captured != null || move.to == position.enPassant) 0
                else position.halfmoves + 1)
    }

    private fun revokeRook(rights: CastlingRights, square: Int) = when (square) {
        0 -> rights.revoke(ChessColor.Black, false)
        7 -> rights.revoke(ChessColor.Black, true)
        56 -> rights.revoke(ChessColor.White, false)
        63 -> rights.revoke(ChessColor.White, true)
        else -> rights
    }

    fun insufficientMaterial(board: List<Piece?>): Boolean {
        val pieces = board.filterNotNull().filter { it.kind != PieceKind.King }
        if (pieces.isEmpty() || pieces.size == 1 && pieces[0].kind in setOf(PieceKind.Bishop, PieceKind.Knight)) return true
        return pieces.size == 2 && pieces.all { it.kind == PieceKind.Bishop } &&
            board.withIndex().filter { it.value?.kind == PieceKind.Bishop }
                .map { (it.index / 8 + it.index % 8) % 2 }.distinct().size == 1
    }

    private fun onBoard(row: Int, col: Int) = row in 0..7 && col in 0..7
}

internal object ChessAi {
    fun choose(game: ChessGame): ChessMove? {
        val moves = game.legalMoves()
        if (moves.isEmpty()) return null
        val depth = when (game.level) { ChessLevel.Easy -> 1; ChessLevel.Medium -> 2; else -> 3 }
        return moves.maxByOrNull { move ->
            val next = ChessRules.apply(game.position, move)
            -search(next, depth - 1, -100_000, 100_000)
        }
    }

    private fun search(position: ChessPosition, depth: Int, initialAlpha: Int, beta: Int): Int {
        if (depth == 0) return evaluate(position)
        val moves = ChessRules.legalMoves(position)
        if (moves.isEmpty()) return if (ChessRules.isInCheck(position.board, position.turn)) -20_000 - depth else 0
        var alpha = initialAlpha
        for (move in moves.sortedByDescending { position.board[it.to]?.kind?.value ?: 0 }) {
            val value = -search(ChessRules.apply(position, move), depth - 1, -beta, -alpha)
            if (value > alpha) alpha = value
            if (alpha >= beta) break
        }
        return alpha
    }

    private fun evaluate(position: ChessPosition): Int = position.board.withIndex().sumOf { (square, piece) ->
        if (piece == null) 0 else {
            val center = 7 - abs(square / 8 - 3) - abs(square % 8 - 3)
            val value = piece.kind.value + if (piece.kind == PieceKind.King) 0 else center * 3
            if (piece.color == position.turn) value else -value
        }
    }
}
