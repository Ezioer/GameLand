package com.zxq.sudoku.games.maze

import kotlin.random.Random

internal enum class MazeDifficulty(val label: String, val size: Int) {
    Easy("简单", 11), Medium("中级", 15), Hard("困难", 21),
}

internal enum class MazeDirection(val dx: Int, val dy: Int) {
    Up(0, -1), Down(0, 1), Left(-1, 0), Right(1, 0),
}

internal data class MazeGame(
    val difficulty: MazeDifficulty,
    val walls: Set<Int>,
    val player: Int,
    val moves: Int = 0,
    val elapsedSeconds: Int = 0,
    val hintsUsed: Int = 0,
    val history: List<Int> = emptyList(),
    val visited: Set<Int> = setOf(player),
    val hintCell: Int? = null,
    val everCompleted: Boolean = false,
) {
    val size get() = difficulty.size
    val start get() = size + 1
    val exit get() = (size - 2) * size + size - 2
    val completed get() = player == exit

    fun neighbor(index: Int, direction: MazeDirection): Int? {
        val x = index % size + direction.dx
        val y = index / size + direction.dy
        if (x !in 0 until size || y !in 0 until size) return null
        return (y * size + x).takeUnless { it in walls }
    }

    fun move(direction: MazeDirection): MazeGame {
        if (completed) return this
        val next = neighbor(player, direction) ?: return this
        return copy(player = next, moves = moves + 1, history = history + player,
            visited = visited + next, hintCell = null,
            everCompleted = everCompleted || next == exit)
    }

    fun undo(): MazeGame {
        val previous = history.lastOrNull() ?: return this
        return copy(player = previous, moves = moves - 1,
            history = history.dropLast(1), hintCell = null)
    }

    fun hint(): MazeGame {
        if (completed) return this
        val next = shortestPath().firstOrNull() ?: return this
        if (next == hintCell) return this
        return copy(hintCell = next, hintsUsed = hintsUsed + 1)
    }

    fun shortestPath(): List<Int> {
        val previous = IntArray(size * size) { -1 }
        val queue = mutableListOf(player)
        previous[player] = player
        var head = 0
        while (head < queue.size && previous[exit] == -1) {
            val current = queue[head++]
            MazeDirection.entries.forEach { direction ->
                val next = neighbor(current, direction)
                if (next != null && previous[next] == -1) {
                    previous[next] = current
                    queue += next
                }
            }
        }
        if (previous[exit] == -1) return emptyList()
        val path = mutableListOf<Int>()
        var current = exit
        while (current != player) {
            path += current
            current = previous[current]
        }
        return path.asReversed()
    }

    fun tick(): MazeGame = if (completed) this else copy(elapsedSeconds = elapsedSeconds + 1)

    fun restart(): MazeGame = copy(player = start, moves = 0, elapsedSeconds = 0,
        hintsUsed = 0, history = emptyList(), visited = setOf(start),
        hintCell = null, everCompleted = false)

    companion object {
        fun start(difficulty: MazeDifficulty,
            nextInt: (Int) -> Int = Random.Default::nextInt): MazeGame {
            val size = difficulty.size
            val walls = (0 until size * size).toMutableSet()
            val start = size + 1
            walls.remove(start)
            val stack = mutableListOf(start)
            while (stack.isNotEmpty()) {
                val current = stack.last()
                val x = current % size
                val y = current / size
                val options = MazeDirection.entries.mapNotNull { direction ->
                    val nx = x + direction.dx * 2
                    val ny = y + direction.dy * 2
                    if (nx !in 1 until size - 1 || ny !in 1 until size - 1) null
                    else (ny * size + nx).takeIf { it in walls }?.let { target ->
                        (y + direction.dy) * size + x + direction.dx to target
                    }
                }
                if (options.isEmpty()) stack.removeAt(stack.lastIndex)
                else {
                    val (between, target) = options[nextInt(options.size)]
                    walls.remove(between)
                    walls.remove(target)
                    stack += target
                }
            }
            return MazeGame(difficulty, walls, start)
        }
    }
}

internal fun MazeGame.serialize(): String = listOf(
    "v1", difficulty.name,
    buildString { repeat(size * size) { append(if (it in walls) '#' else '.') } },
    player, moves, elapsedSeconds, hintsUsed,
    if (everCompleted) 1 else 0,
    history.joinToString(","), visited.sorted().joinToString(","), hintCell ?: -1,
).joinToString("|")

internal fun restoreMazeGame(value: String): MazeGame? = runCatching {
    val fields = value.split('|')
    require(fields.size == 11 && fields[0] == "v1")
    val difficulty = MazeDifficulty.valueOf(fields[1])
    val size = difficulty.size
    require(fields[2].length == size * size && fields[2].all { it == '#' || it == '.' })
    val walls = fields[2].indices.filterTo(mutableSetOf()) { fields[2][it] == '#' }
    require((0 until size).all { col ->
        col in walls && (size - 1) * size + col in walls
    })
    require((0 until size).all { row ->
        row * size in walls && row * size + size - 1 in walls
    })
    fun count(index: Int) = fields[index].toInt().also { require(it >= 0) }
    fun open(index: Int) = index.also { require(it in 0 until size * size && it !in walls) }
    fun cells(text: String) = if (text.isEmpty()) emptyList() else text.split(',').map { open(it.toInt()) }
    val player = open(count(3))
    val history = cells(fields[8])
    val visited = cells(fields[9]).toSet()
    val hint = fields[10].toInt().takeIf { it >= 0 }?.let(::open)
    require(size + 1 !in walls && (size - 2) * size + size - 2 !in walls)
    require(fields[7] == "0" || fields[7] == "1")
    MazeGame(difficulty, walls, player, count(4), count(5), count(6),
        history, visited, hint, fields[7] == "1").also {
        require(it.history.size == it.moves && it.player in it.visited && it.start in it.visited)
    }
}.getOrNull()
