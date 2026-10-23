package com.zxq.sudoku.games.go

import kotlin.math.abs

internal const val GoSize = 9
internal const val GoCells = GoSize * GoSize
internal const val GoKomi = 5.5

internal enum class GoStone(val label: String) {
    Black("黑方"), White("白方");
    fun other() = if (this == Black) White else Black
}

internal enum class GoMode { Human, Friend }
internal enum class GoLevel(val label: String) { Easy("简单"), Medium("中级"), Hard("困难") }
internal enum class GoPhase { Playing, Scoring, Finished }
internal enum class GoEnding { Counted, Resigned, AgreedDraw }

internal data class GoScore(val blackArea: Int, val whiteArea: Int) {
    val blackPoints get() = blackArea.toDouble()
    val whitePoints get() = whiteArea + GoKomi
    val winner get() = when {
        blackPoints > whitePoints -> GoStone.Black
        whitePoints > blackPoints -> GoStone.White
        else -> null
    }
}

internal data class GoPosition(
    val board: List<GoStone?>,
    val turn: GoStone,
    val blackCaptures: Int,
    val whiteCaptures: Int,
    val consecutivePasses: Int,
)

internal data class GoGame(
    val mode: GoMode,
    val level: GoLevel? = null,
    val board: List<GoStone?> = List(GoCells) { null },
    val turn: GoStone = GoStone.Black,
    val blackCaptures: Int = 0,
    val whiteCaptures: Int = 0,
    val consecutivePasses: Int = 0,
    val history: List<GoPosition> = emptyList(),
    val phase: GoPhase = GoPhase.Playing,
    val deadStones: Set<Int> = emptySet(),
    val winner: GoStone? = null,
    val drawn: Boolean = false,
    val ending: GoEnding? = null,
    val score: GoScore? = null,
) {
    val finished get() = phase == GoPhase.Finished
    val moveCount get() = history.size
    val effectiveBoard get() = board.mapIndexed { index, stone -> if (index in deadStones) null else stone }

    private fun snapshot() = GoPosition(board, turn, blackCaptures, whiteCaptures, consecutivePasses)

    fun place(index: Int): GoGame? {
        if (phase != GoPhase.Playing) return null
        val move = GoRules.play(board, index, turn) ?: return null
        if (history.any { it.board == move.board }) return null
        return copy(
            board = move.board,
            turn = turn.other(),
            blackCaptures = blackCaptures + if (turn == GoStone.Black) move.captured else 0,
            whiteCaptures = whiteCaptures + if (turn == GoStone.White) move.captured else 0,
            consecutivePasses = 0,
            history = history + snapshot(),
        )
    }

    fun pass(): GoGame {
        if (phase != GoPhase.Playing) return this
        val passes = consecutivePasses + 1
        return copy(turn = turn.other(), consecutivePasses = passes, history = history + snapshot(),
            phase = if (passes >= 2) GoPhase.Scoring else GoPhase.Playing)
    }

    fun toggleDead(index: Int): GoGame {
        if (phase != GoPhase.Scoring || index !in board.indices || board[index] == null) return this
        val group = GoRules.group(board, index)
        val next = if (index in deadStones) deadStones - group.stones else deadStones + group.stones
        return copy(deadStones = next)
    }

    fun finishScoring(): GoGame {
        if (phase != GoPhase.Scoring) return this
        val counted = GoRules.score(effectiveBoard)
        return copy(phase = GoPhase.Finished, winner = counted.winner, drawn = counted.winner == null,
            ending = GoEnding.Counted, score = counted)
    }

    fun resumePlaying(): GoGame = if (phase == GoPhase.Scoring)
        copy(phase = GoPhase.Playing, consecutivePasses = 0, deadStones = emptySet()) else this

    fun resign(): GoGame = if (phase == GoPhase.Playing) copy(phase = GoPhase.Finished,
        winner = turn.other(), ending = GoEnding.Resigned) else this

    fun agreeDraw(): GoGame = if (phase == GoPhase.Playing) copy(phase = GoPhase.Finished,
        drawn = true, ending = GoEnding.AgreedDraw) else this

    fun undo(): GoGame {
        if (phase != GoPhase.Playing || history.isEmpty()) return this
        val steps = if (mode == GoMode.Human && turn == GoStone.Black && history.size >= 2) 2 else 1
        val previous = history[history.size - steps]
        return copy(board = previous.board, turn = previous.turn,
            blackCaptures = previous.blackCaptures, whiteCaptures = previous.whiteCaptures,
            consecutivePasses = previous.consecutivePasses, history = history.dropLast(steps))
    }
}

internal data class GoGroup(val stones: Set<Int>, val liberties: Set<Int>)
internal data class GoMove(val board: List<GoStone?>, val captured: Int)

internal object GoRules {
    fun neighbors(index: Int): List<Int> {
        val row = index / GoSize
        val column = index % GoSize
        return buildList {
            if (row > 0) add(index - GoSize)
            if (row < GoSize - 1) add(index + GoSize)
            if (column > 0) add(index - 1)
            if (column < GoSize - 1) add(index + 1)
        }
    }

    fun group(board: List<GoStone?>, start: Int): GoGroup {
        val color = board[start] ?: return GoGroup(emptySet(), emptySet())
        val stones = mutableSetOf(start)
        val liberties = mutableSetOf<Int>()
        val pending = ArrayDeque<Int>().apply { add(start) }
        while (pending.isNotEmpty()) {
            for (neighbor in neighbors(pending.removeFirst())) {
                when (board[neighbor]) {
                    color -> if (stones.add(neighbor)) pending.add(neighbor)
                    null -> liberties.add(neighbor)
                    else -> Unit
                }
            }
        }
        return GoGroup(stones, liberties)
    }

    fun play(board: List<GoStone?>, index: Int, color: GoStone): GoMove? {
        if (index !in 0 until GoCells || board[index] != null) return null
        val next = board.toMutableList().apply { this[index] = color }
        var captured = 0
        for (neighbor in neighbors(index)) {
            if (next[neighbor] == color.other()) {
                val opponent = group(next, neighbor)
                if (opponent.liberties.isEmpty()) {
                    captured += opponent.stones.size
                    opponent.stones.forEach { next[it] = null }
                }
            }
        }
        if (group(next, index).liberties.isEmpty()) return null
        return GoMove(next, captured)
    }

    fun score(board: List<GoStone?>): GoScore {
        var black = board.count { it == GoStone.Black }
        var white = board.count { it == GoStone.White }
        val visited = mutableSetOf<Int>()
        for (start in board.indices) {
            if (board[start] != null || !visited.add(start)) continue
            val region = mutableSetOf(start)
            val borders = mutableSetOf<GoStone>()
            val pending = ArrayDeque<Int>().apply { add(start) }
            while (pending.isNotEmpty()) {
                for (neighbor in neighbors(pending.removeFirst())) {
                    val stone = board[neighbor]
                    if (stone == null && visited.add(neighbor)) {
                        region.add(neighbor)
                        pending.add(neighbor)
                    } else if (stone != null) borders.add(stone)
                }
            }
            if (borders == setOf(GoStone.Black)) black += region.size
            if (borders == setOf(GoStone.White)) white += region.size
        }
        return GoScore(black, white)
    }
}

internal object GoAi {
    fun choose(game: GoGame): Int? {
        val level = game.level ?: GoLevel.Medium
        val side = game.turn
        val options = game.board.indices.mapNotNull { index -> game.place(index)?.let { index to it } }
        if (options.isEmpty()) return null
        fun value(index: Int, next: GoGame): Int {
            val capture = if (side == GoStone.Black) next.blackCaptures - game.blackCaptures
                else next.whiteCaptures - game.whiteCaptures
            val liberties = GoRules.group(next.board, index).liberties.size
            val center = 8 - abs(index / GoSize - 4) - abs(index % GoSize - 4)
            val neighbors = GoRules.neighbors(index)
            val connected = neighbors.count { game.board[it] == side }
            return when (level) {
                GoLevel.Easy -> capture * 30 + center * 2 + liberties
                GoLevel.Medium, GoLevel.Hard -> capture * 100 + liberties * 8 + connected * 12 + center * 2
            }
        }
        if (game.consecutivePasses == 1 && game.moveCount >= 40 &&
            options.none { (index, next) -> value(index, next) >= 100 }) return null
        val ranked = options.sortedByDescending { (index, next) -> value(index, next) }
        if (level != GoLevel.Hard) return ranked.first().first
        return ranked.take(8).maxByOrNull { (index, next) ->
            val reply = next.board.indices.mapNotNull { point -> next.place(point)?.let { point to it } }
                .maxOfOrNull { (point, after) ->
                    val captured = if (side == GoStone.Black) after.whiteCaptures - next.whiteCaptures
                        else after.blackCaptures - next.blackCaptures
                    captured * 100 + GoRules.group(after.board, point).liberties.size * 4
                } ?: 0
            value(index, next) - reply
        }?.first
    }

    fun acceptsDraw(game: GoGame): Boolean = game.moveCount >= 30
}
