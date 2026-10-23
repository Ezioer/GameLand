package com.zxq.sudoku.games.game2048

import kotlin.random.Random

internal const val Game2048Size = 4
internal const val Game2048Cells = Game2048Size * Game2048Size

internal enum class SlideDirection { Up, Down, Left, Right }

internal data class Game2048Snapshot(val board: List<Int>, val score: Int,
    val moves: Int, val continuedAfterWin: Boolean)

internal data class Game2048Game(
    val board: List<Int> = List(Game2048Cells) { 0 },
    val score: Int = 0,
    val moves: Int = 0,
    val history: List<Game2048Snapshot> = emptyList(),
    val continuedAfterWin: Boolean = false,
    val everWon: Boolean = false,
    val everFinished: Boolean = false,
) {
    val maxTile get() = board.maxOrNull() ?: 0
    val won get() = maxTile >= 2048 && !continuedAfterWin
    val canMove get() = board.any { it == 0 } || board.indices.any { index ->
        val row = index / Game2048Size
        val column = index % Game2048Size
        (column < Game2048Size - 1 && board[index] == board[index + 1]) ||
            (row < Game2048Size - 1 && board[index] == board[index + Game2048Size])
    }
    val finished get() = !canMove

    fun slide(direction: SlideDirection, nextInt: (Int) -> Int = Random.Default::nextInt): Game2048Game {
        if (finished || won) return this
        val next = MutableList(Game2048Cells) { 0 }
        var gained = 0
        for (line in 0 until Game2048Size) {
            val indices = when (direction) {
                SlideDirection.Left -> (0 until Game2048Size).map { line * Game2048Size + it }
                SlideDirection.Right -> (Game2048Size - 1 downTo 0).map { line * Game2048Size + it }
                SlideDirection.Up -> (0 until Game2048Size).map { it * Game2048Size + line }
                SlideDirection.Down -> (Game2048Size - 1 downTo 0).map { it * Game2048Size + line }
            }
            val values = indices.map { board[it] }.filter { it != 0 }
            val merged = mutableListOf<Int>()
            var position = 0
            while (position < values.size) {
                if (position + 1 < values.size && values[position] == values[position + 1]) {
                    val value = values[position] * 2
                    merged.add(value)
                    gained += value
                    position += 2
                } else {
                    merged.add(values[position])
                    position++
                }
            }
            merged.forEachIndexed { index, value -> next[indices[index]] = value }
        }
        if (next == board) return this
        val previous = Game2048Snapshot(board, score, moves, continuedAfterWin)
        val moved = copy(board = next, score = score + gained, moves = moves + 1,
            history = history + previous)
        val spawned = moved.spawn(nextInt)
        return spawned.copy(everWon = everWon || spawned.maxTile >= 2048,
            everFinished = everFinished || spawned.finished)
    }

    fun undo(): Game2048Game {
        val previous = history.lastOrNull() ?: return this
        return copy(board = previous.board, score = previous.score, moves = previous.moves,
            history = history.dropLast(1), continuedAfterWin = previous.continuedAfterWin)
    }

    fun continuePlaying(): Game2048Game = if (won) copy(continuedAfterWin = true) else this

    private fun spawn(nextInt: (Int) -> Int): Game2048Game {
        val empty = board.indices.filter { board[it] == 0 }
        if (empty.isEmpty()) return this
        val index = empty[nextInt(empty.size)]
        val value = if (nextInt(10) == 0) 4 else 2
        return copy(board = board.toMutableList().apply { this[index] = value })
    }

    companion object {
        fun start(nextInt: (Int) -> Int = Random.Default::nextInt): Game2048Game =
            Game2048Game().spawn(nextInt).spawn(nextInt)
    }
}

internal fun Game2048Game.serialize(): String = buildString {
    append("v1|")
    append(board.joinToString(","))
    append('|').append(score)
    append('|').append(moves)
    append('|').append(if (continuedAfterWin) 1 else 0)
    append('|').append(if (everWon) 1 else 0)
    append('|').append(if (everFinished) 1 else 0)
    append('|')
    append(history.joinToString(";") { snapshot ->
        "${snapshot.board.joinToString(",")}:${snapshot.score}:${snapshot.moves}:${if (snapshot.continuedAfterWin) 1 else 0}"
    })
}

internal fun restoreGame2048(value: String): Game2048Game? = runCatching {
    val fields = value.split('|')
    require(fields.size == 8 && fields[0] == "v1")
    fun board(text: String): List<Int> = text.split(',').map { cell ->
        cell.toInt().also { require(it == 0 || (it >= 2 && (it and (it - 1)) == 0)) }
    }.also { require(it.size == Game2048Cells) }
    fun nonNegative(text: String) = text.toInt().also { require(it >= 0) }
    fun flag(text: String) = when (text) { "0" -> false; "1" -> true; else -> error("Invalid flag") }
    val history = if (fields[7].isEmpty()) emptyList() else fields[7].split(';').map { entry ->
        val parts = entry.split(':')
        require(parts.size == 4)
        Game2048Snapshot(board(parts[0]), nonNegative(parts[1]), nonNegative(parts[2]), flag(parts[3]))
    }
    Game2048Game(board(fields[1]), nonNegative(fields[2]), nonNegative(fields[3]), history,
        flag(fields[4]), flag(fields[5]), flag(fields[6]))
}.getOrNull()
