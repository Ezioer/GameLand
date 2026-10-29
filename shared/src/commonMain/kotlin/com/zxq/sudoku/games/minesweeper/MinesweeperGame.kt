package com.zxq.sudoku.games.minesweeper

import kotlin.random.Random

internal enum class MinesweeperDifficulty(
    val label: String, val columns: Int, val rows: Int, val mineCount: Int,
) {
    Easy("简单", 9, 9, 10),
    Medium("中级", 16, 16, 40),
    Hard("困难", 30, 16, 99),
}

internal enum class MinesweeperStatus { Playing, Won, Lost }

internal data class MinesweeperGame(
    val difficulty: MinesweeperDifficulty,
    val mines: Set<Int> = emptySet(),
    val revealed: Set<Int> = emptySet(),
    val flags: Set<Int> = emptySet(),
    val elapsedSeconds: Int = 0,
    val status: MinesweeperStatus = MinesweeperStatus.Playing,
    val exploded: Int? = null,
) {
    val cellCount get() = difficulty.columns * difficulty.rows
    val started get() = mines.isNotEmpty()
    val minesRemaining get() = difficulty.mineCount - flags.size

    fun neighbors(index: Int): List<Int> {
        if (index !in 0 until cellCount) return emptyList()
        val x = index % difficulty.columns
        val y = index / difficulty.columns
        return (-1..1).flatMap { dy ->
            (-1..1).mapNotNull { dx ->
                val nx = x + dx
                val ny = y + dy
                if ((dx == 0 && dy == 0) || nx !in 0 until difficulty.columns ||
                    ny !in 0 until difficulty.rows) null
                else ny * difficulty.columns + nx
            }
        }
    }

    fun adjacentMines(index: Int): Int = neighbors(index).count { it in mines }

    fun toggleFlag(index: Int): MinesweeperGame {
        if (status != MinesweeperStatus.Playing || index !in 0 until cellCount || index in revealed) return this
        return copy(flags = if (index in flags) flags - index else flags + index)
    }

    fun reveal(index: Int, nextInt: (Int) -> Int = Random.Default::nextInt): MinesweeperGame {
        if (status != MinesweeperStatus.Playing || index !in 0 until cellCount ||
            index in revealed || index in flags) return this
        val board = if (started) this else copy(mines = placeMines(index, nextInt))
        if (index in board.mines) return board.copy(status = MinesweeperStatus.Lost, exploded = index)
        val opened = board.revealed.toMutableSet()
        val queue = mutableListOf(index)
        var head = 0
        while (head < queue.size) {
            val cell = queue[head++]
            if (cell in opened || cell in board.flags || cell in board.mines) continue
            opened += cell
            if (board.adjacentMines(cell) == 0) queue += board.neighbors(cell)
        }
        return board.copy(revealed = opened,
            status = if (opened.size == cellCount - difficulty.mineCount) MinesweeperStatus.Won
                else MinesweeperStatus.Playing)
    }

    fun chord(index: Int): MinesweeperGame {
        if (status != MinesweeperStatus.Playing || index !in revealed) return this
        val around = neighbors(index)
        val count = adjacentMines(index)
        if (count == 0 || around.count { it in flags } != count) return this
        return around.filter { it !in flags && it !in revealed }
            .fold(this) { game, cell -> game.reveal(cell) }
    }

    fun tick(): MinesweeperGame =
        if (status == MinesweeperStatus.Playing && started) copy(elapsedSeconds = elapsedSeconds + 1)
        else this

    private fun placeMines(first: Int, nextInt: (Int) -> Int): Set<Int> {
        val safe = neighbors(first).toSet() + first
        val candidates = (0 until cellCount).filterNot { it in safe }.toMutableList()
        repeat(difficulty.mineCount) { position ->
            val swap = position + nextInt(candidates.size - position)
            val value = candidates[position]
            candidates[position] = candidates[swap]
            candidates[swap] = value
        }
        return candidates.take(difficulty.mineCount).toSet()
    }

    companion object {
        fun start(difficulty: MinesweeperDifficulty) = MinesweeperGame(difficulty)
    }
}

internal fun MinesweeperGame.serialize(): String = listOf(
    "v1", difficulty.name, mines.sorted().joinToString(","),
    revealed.sorted().joinToString(","), flags.sorted().joinToString(","),
    elapsedSeconds, status.name, exploded ?: -1,
).joinToString("|")

internal fun restoreMinesweeperGame(value: String): MinesweeperGame? = runCatching {
    val fields = value.split('|')
    require(fields.size == 8 && fields[0] == "v1")
    val difficulty = MinesweeperDifficulty.valueOf(fields[1])
    val count = difficulty.columns * difficulty.rows
    fun cells(text: String): Set<Int> = if (text.isEmpty()) emptySet() else
        text.split(',').map { it.toInt().also { cell -> require(cell in 0 until count) } }
            .toSet().also { require(it.size == text.split(',').size) }
    val mines = cells(fields[2])
    val revealed = cells(fields[3])
    val flags = cells(fields[4])
    val seconds = fields[5].toInt().also { require(it >= 0) }
    val status = MinesweeperStatus.valueOf(fields[6])
    val exploded = fields[7].toInt().takeIf { it >= 0 }
    require(mines.isEmpty() || mines.size == difficulty.mineCount)
    require(mines.isNotEmpty() || revealed.isEmpty())
    require(flags.intersect(revealed).isEmpty())
    require(mines.intersect(revealed).isEmpty())
    require((status == MinesweeperStatus.Lost) == (exploded != null))
    require(exploded == null || exploded in mines)
    require(status != MinesweeperStatus.Won || revealed.size == count - difficulty.mineCount)
    require(status == MinesweeperStatus.Playing || mines.isNotEmpty())
    MinesweeperGame(difficulty, mines, revealed, flags, seconds, status, exploded)
}.getOrNull()
