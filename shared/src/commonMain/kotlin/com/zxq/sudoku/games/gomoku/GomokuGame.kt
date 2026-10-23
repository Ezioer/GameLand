package com.zxq.sudoku.games.gomoku

import kotlin.math.abs

internal const val GomokuSize = 15
internal const val GomokuCells = GomokuSize * GomokuSize

internal enum class GomokuStone(val label: String) {
    Black("黑方"), White("白方");
    fun other() = if (this == Black) White else Black
}

internal enum class GomokuMode { Human, Friend }
internal enum class GomokuLevel(val label: String) { Easy("简单"), Medium("中级"), Hard("困难") }

internal data class GomokuGame(
    val mode: GomokuMode,
    val level: GomokuLevel? = null,
    val board: List<GomokuStone?> = List(GomokuCells) { null },
    val moves: List<Int> = emptyList(),
    val turn: GomokuStone = GomokuStone.Black,
    val winner: GomokuStone? = null,
    val drawn: Boolean = false,
) {
    val finished get() = winner != null || drawn

    fun place(index: Int): GomokuGame? {
        if (finished || index !in board.indices || board[index] != null) return null
        val next = board.toMutableList().apply { this[index] = turn }
        val won = GomokuRules.hasFive(next, index, turn)
        return copy(
            board = next,
            moves = moves + index,
            turn = turn.other(),
            winner = turn.takeIf { won },
            drawn = !won && next.none { it == null },
        )
    }

    fun undo(): GomokuGame {
        if (finished || moves.isEmpty()) return this
        val steps = if (mode == GomokuMode.Human && turn == GomokuStone.Black && moves.size >= 2) 2 else 1
        val removed = moves.takeLast(steps)
        val remaining = moves.dropLast(steps)
        val previous = board.toMutableList().apply { removed.forEach { this[it] = null } }
        return copy(board = previous, moves = remaining,
            turn = if (remaining.size % 2 == 0) GomokuStone.Black else GomokuStone.White)
    }

    fun resign() = if (finished) this else copy(winner = turn.other())
    fun agreeDraw() = if (finished) this else copy(drawn = true)
}

internal object GomokuRules {
    private val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)

    fun hasFive(board: List<GomokuStone?>, index: Int, stone: GomokuStone): Boolean {
        if (index !in board.indices || board[index] != stone) return false
        val row = index / GomokuSize
        val column = index % GomokuSize
        return directions.any { (dr, dc) ->
            1 + count(board, row, column, dr, dc, stone).first +
                count(board, row, column, -dr, -dc, stone).first >= 5
        }
    }

    fun patternScore(board: List<GomokuStone?>, index: Int, stone: GomokuStone): Int {
        if (index !in board.indices || board[index] != null) return 0
        val row = index / GomokuSize
        val column = index % GomokuSize
        return directions.sumOf { (dr, dc) ->
            val forward = count(board, row, column, dr, dc, stone)
            val backward = count(board, row, column, -dr, -dc, stone)
            val length = 1 + forward.first + backward.first
            val open = (if (forward.second) 1 else 0) + (if (backward.second) 1 else 0)
            when {
                length >= 5 -> 1_000_000
                length == 4 && open == 2 -> 20_000
                length == 4 && open == 1 -> 4_000
                length == 3 && open == 2 -> 2_000
                length == 3 && open == 1 -> 300
                length == 2 && open == 2 -> 120
                length == 2 && open == 1 -> 30
                else -> 4
            }
        }
    }

    fun candidates(board: List<GomokuStone?>): List<Int> {
        if (board.none { it != null }) return listOf(GomokuCells / 2)
        return board.indices.filter { index ->
            board[index] == null && (-2..2).any { dr ->
                (-2..2).any { dc ->
                    val row = index / GomokuSize + dr
                    val column = index % GomokuSize + dc
                    row in 0 until GomokuSize && column in 0 until GomokuSize &&
                        board[row * GomokuSize + column] != null
                }
            }
        }
    }

    private fun count(board: List<GomokuStone?>, row: Int, column: Int, dr: Int, dc: Int,
        stone: GomokuStone): Pair<Int, Boolean> {
        var r = row + dr
        var c = column + dc
        var count = 0
        while (r in 0 until GomokuSize && c in 0 until GomokuSize && board[r * GomokuSize + c] == stone) {
            count++
            r += dr
            c += dc
        }
        return count to (r in 0 until GomokuSize && c in 0 until GomokuSize && board[r * GomokuSize + c] == null)
    }
}

internal object GomokuAi {
    fun choose(board: List<GomokuStone?>, side: GomokuStone, level: GomokuLevel): Int? {
        val candidates = GomokuRules.candidates(board)
        if (candidates.isEmpty()) return null
        fun score(index: Int): Int {
            val attack = GomokuRules.patternScore(board, index, side)
            val defense = GomokuRules.patternScore(board, index, side.other())
            val center = 14 - abs(index / GomokuSize - 7) - abs(index % GomokuSize - 7)
            return when (level) {
                GomokuLevel.Easy -> attack + center * 3
                GomokuLevel.Medium, GomokuLevel.Hard -> attack * 2 + defense * 2 + center
            }
        }
        if (level != GomokuLevel.Easy) {
            candidates.firstOrNull { wins(board, it, side) }?.let { return it }
            candidates.firstOrNull { wins(board, it, side.other()) }?.let { return it }
        }
        if (level != GomokuLevel.Hard) return candidates.maxByOrNull(::score)
        return candidates.sortedByDescending(::score).take(8).maxByOrNull { index ->
            val next = board.toMutableList().apply { this[index] = side }
            val reply = GomokuRules.candidates(next).maxOfOrNull {
                GomokuRules.patternScore(next, it, side.other())
            } ?: 0
            score(index) - reply * 2
        }
    }

    fun acceptsDraw(game: GomokuGame): Boolean = game.moves.size >= 40 &&
        GomokuRules.candidates(game.board).none { index ->
            wins(game.board, index, GomokuStone.Black) || wins(game.board, index, GomokuStone.White)
        }

    private fun wins(board: List<GomokuStone?>, index: Int, stone: GomokuStone): Boolean {
        val next = board.toMutableList().apply { this[index] = stone }
        return GomokuRules.hasFive(next, index, stone)
    }
}
