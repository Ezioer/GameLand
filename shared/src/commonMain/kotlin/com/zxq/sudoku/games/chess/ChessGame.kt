package com.zxq.sudoku.games.chess

import kotlin.math.abs

internal enum class ChessSide { Red, Black;
    fun other() = if (this == Red) Black else Red
    val label get() = if (this == Red) "红方" else "黑方"
}

internal enum class ChessPieceType { General, Advisor, Elephant, Horse, Rook, Cannon, Pawn }
internal data class ChessPiece(val side: ChessSide, val type: ChessPieceType) {
    val label: String get() = when (type) {
        ChessPieceType.General -> if (side == ChessSide.Red) "帅" else "将"
        ChessPieceType.Advisor -> if (side == ChessSide.Red) "仕" else "士"
        ChessPieceType.Elephant -> if (side == ChessSide.Red) "相" else "象"
        ChessPieceType.Horse -> "马"
        ChessPieceType.Rook -> "车"
        ChessPieceType.Cannon -> "炮"
        ChessPieceType.Pawn -> if (side == ChessSide.Red) "兵" else "卒"
    }
}

internal data class ChessMove(val from: Int, val to: Int)
internal enum class ChessMode { Human, Friend, Puzzle }
internal enum class ChessLevel(val label: String, val depth: Int) {
    Easy("简单", 1), Medium("中级", 2), Hard("困难", 3),
}

internal data class ChessPuzzle(val title: String, val description: String, val board: List<ChessPiece?>)

internal data class ChessGame(
    val board: List<ChessPiece?>,
    val turn: ChessSide = ChessSide.Red,
    val mode: ChessMode,
    val level: ChessLevel? = null,
    val puzzleIndex: Int? = null,
    val history: List<Pair<List<ChessPiece?>, ChessSide>> = emptyList(),
    val winner: ChessSide? = null,
    val drawn: Boolean = false,
) {
    val finished get() = winner != null || drawn
    fun legalMoves(from: Int): List<ChessMove> = ChessRules.legalMoves(board, turn).filter { it.from == from }
    fun move(move: ChessMove): ChessGame? {
        if (finished || move !in ChessRules.legalMoves(board, turn)) return null
        val nextBoard = ChessRules.apply(board, move)
        val nextTurn = turn.other()
        val won = ChessRules.legalMoves(nextBoard, nextTurn).isEmpty() ||
            nextBoard.none { it?.side == nextTurn && it.type == ChessPieceType.General }
        return copy(
            board = nextBoard,
            turn = nextTurn,
            history = history + (board to turn),
            winner = if (won) turn else null,
            drawn = false,
        )
    }
    fun undo(): ChessGame {
        val steps = if (mode == ChessMode.Human && turn == ChessSide.Red) 2 else 1
        if (history.size < steps) return this
        val previous = history[history.size - steps]
        return copy(board = previous.first, turn = previous.second, history = history.dropLast(steps), winner = null, drawn = false)
    }
    fun resign() = copy(winner = turn.other())
    fun agreeDraw() = if (finished) this else copy(drawn = true)
}

internal object ChessRules {
    private fun row(i: Int) = i / 9
    private fun col(i: Int) = i % 9
    private fun inside(r: Int, c: Int) = r in 0..9 && c in 0..8
    private fun palace(side: ChessSide, r: Int, c: Int) = c in 3..5 &&
        r in (if (side == ChessSide.Red) 7..9 else 0..2)
    private fun crossRiver(side: ChessSide, r: Int) = if (side == ChessSide.Red) r <= 4 else r >= 5

    fun initialBoard(): List<ChessPiece?> = MutableList<ChessPiece?>(90) { null }.apply {
        val back = listOf(ChessPieceType.Rook, ChessPieceType.Horse, ChessPieceType.Elephant,
            ChessPieceType.Advisor, ChessPieceType.General, ChessPieceType.Advisor,
            ChessPieceType.Elephant, ChessPieceType.Horse, ChessPieceType.Rook)
        for (c in 0..8) {
            this[c] = ChessPiece(ChessSide.Black, back[c])
            this[9 * 9 + c] = ChessPiece(ChessSide.Red, back[c])
        }
        for (c in listOf(1, 7)) {
            this[2 * 9 + c] = ChessPiece(ChessSide.Black, ChessPieceType.Cannon)
            this[7 * 9 + c] = ChessPiece(ChessSide.Red, ChessPieceType.Cannon)
        }
        for (c in 0..8 step 2) {
            this[3 * 9 + c] = ChessPiece(ChessSide.Black, ChessPieceType.Pawn)
            this[6 * 9 + c] = ChessPiece(ChessSide.Red, ChessPieceType.Pawn)
        }
    }

    fun apply(board: List<ChessPiece?>, move: ChessMove): List<ChessPiece?> = board.toMutableList().apply {
        this[move.to] = this[move.from]
        this[move.from] = null
    }

    private fun pseudoMoves(board: List<ChessPiece?>, from: Int): List<ChessMove> {
        val piece = board[from] ?: return emptyList()
        val r = row(from); val c = col(from)
        val result = mutableListOf<ChessMove>()
        fun add(rr: Int, cc: Int) {
            if (inside(rr, cc) && board[rr * 9 + cc]?.side != piece.side) result += ChessMove(from, rr * 9 + cc)
        }
        when (piece.type) {
            ChessPieceType.General -> {
                for ((dr, dc) in orthogonal) if (palace(piece.side, r + dr, c + dc)) add(r + dr, c + dc)
                val enemy = board.indexOfFirst { it?.side == piece.side.other() && it.type == ChessPieceType.General }
                if (enemy >= 0 && col(enemy) == c && countBetween(board, from, enemy) == 0) add(row(enemy), c)
            }
            ChessPieceType.Advisor -> for (dr in listOf(-1, 1)) for (dc in listOf(-1, 1))
                if (palace(piece.side, r + dr, c + dc)) add(r + dr, c + dc)
            ChessPieceType.Elephant -> for (dr in listOf(-2, 2)) for (dc in listOf(-2, 2)) {
                val rr = r + dr; val cc = c + dc
                if (inside(rr, cc) && !crossRiver(piece.side, rr) && board[(r + dr / 2) * 9 + c + dc / 2] == null) add(rr, cc)
            }
            ChessPieceType.Horse -> for ((dr, dc) in horseSteps) {
                val legR = r + if (abs(dr) == 2) dr / 2 else 0
                val legC = c + if (abs(dc) == 2) dc / 2 else 0
                if (inside(legR, legC) && board[legR * 9 + legC] == null) add(r + dr, c + dc)
            }
            ChessPieceType.Rook, ChessPieceType.Cannon -> for ((dr, dc) in orthogonal) {
                var rr = r + dr; var cc = c + dc; var screened = false
                while (inside(rr, cc)) {
                    val target = board[rr * 9 + cc]
                    if (piece.type == ChessPieceType.Rook) {
                        if (target?.side != piece.side) add(rr, cc)
                        if (target != null) break
                    } else if (!screened) {
                        if (target == null) add(rr, cc) else screened = true
                    } else if (target != null) {
                        if (target.side != piece.side) add(rr, cc)
                        break
                    }
                    rr += dr; cc += dc
                }
            }
            ChessPieceType.Pawn -> {
                add(r + if (piece.side == ChessSide.Red) -1 else 1, c)
                if (crossRiver(piece.side, r)) { add(r, c - 1); add(r, c + 1) }
            }
        }
        return result
    }

    private val orthogonal = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    private val horseSteps = listOf(-2 to -1, -2 to 1, 2 to -1, 2 to 1,
        -1 to -2, 1 to -2, -1 to 2, 1 to 2)

    private fun countBetween(board: List<ChessPiece?>, a: Int, b: Int): Int {
        if (col(a) != col(b)) return Int.MAX_VALUE
        return ((minOf(row(a), row(b)) + 1) until maxOf(row(a), row(b))).count { board[it * 9 + col(a)] != null }
    }

    fun isInCheck(board: List<ChessPiece?>, side: ChessSide): Boolean {
        val general = board.indexOfFirst { it?.side == side && it.type == ChessPieceType.General }
        if (general < 0) return true
        return board.indices.any { i ->
            board[i]?.side == side.other() && pseudoMoves(board, i).any { it.to == general }
        }
    }

    fun legalMoves(board: List<ChessPiece?>, side: ChessSide): List<ChessMove> = buildList {
        for (i in board.indices) if (board[i]?.side == side) {
            for (move in pseudoMoves(board, i)) if (!isInCheck(apply(board, move), side)) add(move)
        }
    }
}

internal object ChessAi {
    private val values = mapOf(
        ChessPieceType.General to 10000, ChessPieceType.Rook to 900, ChessPieceType.Cannon to 450,
        ChessPieceType.Horse to 400, ChessPieceType.Elephant to 200,
        ChessPieceType.Advisor to 200, ChessPieceType.Pawn to 100,
    )

    fun acceptsDraw(game: ChessGame): Boolean = game.history.size >= 20 &&
        evaluate(game.board, ChessSide.Black) <= 0

    fun choose(board: List<ChessPiece?>, side: ChessSide, level: ChessLevel): ChessMove? {
        val moves = ChessRules.legalMoves(board, side)
        return moves.maxByOrNull { move ->
            val next = ChessRules.apply(board, move)
            val score = search(next, side.other(), level.depth - 1, side, -100000, 100000)
            score + ((move.from * 17 + move.to * 7) % 11)
        }
    }

    private fun search(board: List<ChessPiece?>, turn: ChessSide, depth: Int, ai: ChessSide,
        alphaStart: Int, betaStart: Int): Int {
        if (depth == 0) return evaluate(board, ai)
        val moves = ChessRules.legalMoves(board, turn)
        if (moves.isEmpty()) return if (turn == ai) -50000 - depth else 50000 + depth
        var alpha = alphaStart; var beta = betaStart
        if (turn == ai) {
            for (move in moves) {
                alpha = maxOf(alpha, search(ChessRules.apply(board, move), turn.other(), depth - 1, ai, alpha, beta))
                if (alpha >= beta) break
            }
            return alpha
        }
        for (move in moves) {
            beta = minOf(beta, search(ChessRules.apply(board, move), turn.other(), depth - 1, ai, alpha, beta))
            if (alpha >= beta) break
        }
        return beta
    }

    private fun evaluate(board: List<ChessPiece?>, ai: ChessSide): Int = board.sumOf { piece ->
        if (piece == null) 0 else (values.getValue(piece.type) * if (piece.side == ai) 1 else -1)
    }
}

internal object ChessPuzzles {
    val entries: List<ChessPuzzle> = listOf(
        puzzle("双车逼宫", "红先行，用双车封住黑将的退路。",
            9 * 9 + 4 to ChessPieceType.General, 5 * 9 + 4 to ChessPieceType.Pawn,
            2 * 9 + 2 to ChessPieceType.Rook, 2 * 9 + 6 to ChessPieceType.Rook),
        puzzle("马炮联攻", "红先行，让马炮配合形成连续将军。",
            9 * 9 + 4 to ChessPieceType.General, 5 * 9 + 4 to ChessPieceType.Pawn,
            4 * 9 + 3 to ChessPieceType.Horse, 5 * 9 + 1 to ChessPieceType.Cannon,
            7 * 9 + 7 to ChessPieceType.Rook),
        puzzle("单车破城", "红先行，试着用车和兵限制黑将。",
            9 * 9 + 4 to ChessPieceType.General, 4 * 9 + 4 to ChessPieceType.Pawn,
            2 * 9 + 1 to ChessPieceType.Rook, 3 * 9 + 6 to ChessPieceType.Pawn),
    )

    private fun puzzle(title: String, description: String, vararg red: Pair<Int, ChessPieceType>): ChessPuzzle {
        val board = MutableList<ChessPiece?>(90) { null }
        board[4] = ChessPiece(ChessSide.Black, ChessPieceType.General)
        for ((index, type) in red) {
            board[index] = ChessPiece(ChessSide.Red, type)
        }
        return ChessPuzzle(title, description, board)
    }
}

internal fun newChessGame(mode: ChessMode, level: ChessLevel? = null, puzzleIndex: Int? = null): ChessGame = ChessGame(
    board = if (mode == ChessMode.Puzzle) ChessPuzzles.entries[puzzleIndex ?: 0].board else ChessRules.initialBoard(),
    mode = mode,
    level = level,
    puzzleIndex = puzzleIndex,
)
