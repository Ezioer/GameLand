package com.zxq.sudoku.games.sokoban

internal enum class SokobanDirection(val dx: Int, val dy: Int) {
    Up(0, -1), Down(0, 1), Left(-1, 0), Right(1, 0),
}

internal data class SokobanLevel(
    val title: String,
    val width: Int,
    val height: Int,
    val walls: Set<Int>,
    val goals: Set<Int>,
    val initialCrates: Set<Int>,
    val initialPlayer: Int,
) {
    fun next(index: Int, direction: SokobanDirection): Int? {
        val x = index % width + direction.dx
        val y = index / width + direction.dy
        if (x !in 0 until width || y !in 0 until height) return null
        return (y * width + x).takeUnless { it in walls }
    }
}

private fun level(title: String, vararg rows: String): SokobanLevel {
    val width = rows.first().length
    require(rows.all { it.length == width })
    val walls = mutableSetOf<Int>()
    val goals = mutableSetOf<Int>()
    val crates = mutableSetOf<Int>()
    var player = -1
    rows.forEachIndexed { y, row -> row.forEachIndexed { x, cell ->
        val index = y * width + x
        when (cell) {
            '#' -> walls.add(index)
            '.' -> goals.add(index)
            '$' -> crates.add(index)
            '*' -> { goals.add(index); crates.add(index) }
            '@' -> player = index
            '+' -> { goals.add(index); player = index }
            else -> require(cell == ' ')
        }
    } }
    require(player >= 0 && crates.isNotEmpty() && crates.size == goals.size)
    return SokobanLevel(title, width, rows.size, walls, goals, crates, player)
}

internal val SokobanLevels = listOf(
    level("初次推箱", "#######", "#     #", "#  .  #", "#  $  #", "#  @  #", "#     #", "#######"),
    level("双箱协作", "#######", "#  .. #", "#  $$ #", "#  @  #", "#     #", "#######"),
    level("绕个弯", "########", "#      #", "#   .  #", "#  $   #", "#  @   #", "#      #", "########"),
    level("排兵布阵", "########", "#  . . #", "#  $ $ #", "#   @  #", "#      #", "########"),
    level("墙边转身", "########", "#  .   #", "#  #   #", "#  $   #", "#  @   #", "#      #", "########"),
    level("最终挑战", "#########", "#  . .  #", "#  # #  #", "#  $ $  #", "#   @   #", "#       #", "#########"),
)

internal data class SokobanSnapshot(
    val player: Int,
    val crates: Set<Int>,
    val moves: Int,
    val pushes: Int,
)

internal data class SokobanGame(
    val levelIndex: Int,
    val player: Int,
    val crates: Set<Int>,
    val moves: Int = 0,
    val pushes: Int = 0,
    val elapsedSeconds: Int = 0,
    val history: List<SokobanSnapshot> = emptyList(),
    val everCompleted: Boolean = false,
) {
    val level get() = SokobanLevels[levelIndex]
    val completed get() = crates == level.goals

    fun move(direction: SokobanDirection): SokobanGame {
        if (completed) return this
        val next = level.next(player, direction) ?: return this
        val crateTarget = if (next in crates) level.next(next, direction) ?: return this else null
        if (crateTarget != null && crateTarget in crates) return this
        val newCrates = if (crateTarget == null) crates else crates - next + crateTarget
        return copy(player = next, crates = newCrates, moves = moves + 1,
            pushes = pushes + if (crateTarget == null) 0 else 1,
            history = history + SokobanSnapshot(player, crates, moves, pushes),
            everCompleted = everCompleted || newCrates == level.goals)
    }

    fun undo(): SokobanGame {
        val previous = history.lastOrNull() ?: return this
        return copy(player = previous.player, crates = previous.crates, moves = previous.moves,
            pushes = previous.pushes, history = history.dropLast(1))
    }

    fun tick(): SokobanGame = if (completed) this else copy(elapsedSeconds = elapsedSeconds + 1)

    companion object {
        fun start(levelIndex: Int): SokobanGame {
            val level = SokobanLevels[levelIndex]
            return SokobanGame(levelIndex, level.initialPlayer, level.initialCrates)
        }
    }
}

internal fun SokobanGame.serialize(): String = buildString {
    append("v1|").append(levelIndex).append('|').append(player).append('|')
    append(crates.sorted().joinToString(",")).append('|')
    append(moves).append('|').append(pushes).append('|').append(elapsedSeconds).append('|')
    append(if (everCompleted) 1 else 0).append('|')
    append(history.joinToString(";") {
        "${it.player}:${it.crates.sorted().joinToString(",")}:${it.moves}:${it.pushes}"
    })
}

internal fun restoreSokobanGame(value: String): SokobanGame? = runCatching {
    val parts = value.split('|')
    require(parts.size == 9 && parts[0] == "v1")
    val levelIndex = parts[1].toInt()
    val level = SokobanLevels[levelIndex]
    fun number(text: String) = text.toInt().also { require(it >= 0) }
    fun crates(text: String): Set<Int> = text.split(',').map { it.toInt() }.toSet()
        .also { require(it.size == level.goals.size && it.all { cell -> cell in 0 until level.width * level.height && cell !in level.walls }) }
    fun player(text: String) = text.toInt().also {
        require(it in 0 until level.width * level.height && it !in level.walls)
    }
    val history = if (parts[8].isEmpty()) emptyList() else parts[8].split(';').map { entry ->
        val fields = entry.split(':')
        require(fields.size == 4)
        SokobanSnapshot(player(fields[0]), crates(fields[1]), number(fields[2]), number(fields[3]))
    }
    require(parts[7] == "0" || parts[7] == "1")
    SokobanGame(levelIndex, player(parts[2]), crates(parts[3]), number(parts[4]),
        number(parts[5]), number(parts[6]), history, parts[7] == "1").also {
        require(it.player !in it.crates && history.all { step -> step.player !in step.crates })
    }
}.getOrNull()
