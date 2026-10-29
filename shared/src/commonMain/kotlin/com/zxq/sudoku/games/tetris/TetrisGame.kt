package com.zxq.sudoku.games.tetris

import kotlin.random.Random

internal const val TetrisWidth = 10
internal const val TetrisHeight = 20
internal const val TetrisCells = TetrisWidth * TetrisHeight

internal enum class Tetromino { I, O, T, S, Z, J, L, G }

internal data class FallingPiece(
    val kind: Tetromino,
    val rotation: Int = 0,
    val x: Int = 3,
    val y: Int = -1,
) {
    val cells: List<Pair<Int, Int>> get() {
        var points = when (kind) {
            Tetromino.I -> listOf(0 to 1, 1 to 1, 2 to 1, 3 to 1)
            Tetromino.O -> listOf(1 to 0, 2 to 0, 1 to 1, 2 to 1)
            Tetromino.T -> listOf(1 to 0, 0 to 1, 1 to 1, 2 to 1)
            Tetromino.S -> listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1)
            Tetromino.Z -> listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1)
            Tetromino.J -> listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1)
            Tetromino.L -> listOf(2 to 0, 0 to 1, 1 to 1, 2 to 1)
            Tetromino.G -> error("Garbage cannot be a falling piece")
        }
        if (kind != Tetromino.O) repeat(rotation.mod(4)) {
            points = points.map { (col, row) -> (3 - row) to col }
        }
        return points.map { (col, row) -> (x + col) to (y + row) }
    }
}

internal data class TetrisGame(
    val board: List<Tetromino?>,
    val active: FallingPiece,
    val next: List<Tetromino>,
    val bag: List<Tetromino>,
    val held: Tetromino? = null,
    val canHold: Boolean = true,
    val score: Int = 0,
    val lines: Int = 0,
    val pieces: Int = 0,
    val elapsedSeconds: Int = 0,
    val paused: Boolean = false,
    val finished: Boolean = false,
) {
    val level get() = lines / 10 + 1
    val playable get() = !paused && !finished

    fun fits(piece: FallingPiece): Boolean = piece.cells.all { (col, row) ->
        col in 0 until TetrisWidth && row < TetrisHeight &&
            (row < 0 || board[row * TetrisWidth + col] == null)
    }

    fun move(dx: Int): TetrisGame {
        if (!playable) return this
        val moved = active.copy(x = active.x + dx)
        return if (fits(moved)) copy(active = moved) else this
    }

    fun rotate(clockwise: Boolean = true): TetrisGame {
        if (!playable || active.kind == Tetromino.O) return this
        val rotated = active.copy(rotation = (active.rotation + if (clockwise) 1 else 3) % 4)
        for ((dx, dy) in listOf(0 to 0, -1 to 0, 1 to 0, -2 to 0, 2 to 0,
            0 to -1, -1 to -1, 1 to -1, 0 to -2)) {
            val kicked = rotated.copy(x = rotated.x + dx, y = rotated.y + dy)
            if (fits(kicked)) return copy(active = kicked)
        }
        return this
    }

    fun tick(nextInt: (Int) -> Int = Random.Default::nextInt): TetrisGame {
        if (!playable) return this
        val moved = active.copy(y = active.y + 1)
        return if (fits(moved)) copy(active = moved) else lock(nextInt)
    }

    fun softDrop(nextInt: (Int) -> Int = Random.Default::nextInt): TetrisGame {
        if (!playable) return this
        val moved = active.copy(y = active.y + 1)
        return if (fits(moved)) copy(active = moved, score = score + 1) else lock(nextInt)
    }

    fun hardDrop(nextInt: (Int) -> Int = Random.Default::nextInt): TetrisGame {
        if (!playable) return this
        var dropped = active
        var distance = 0
        while (fits(dropped.copy(y = dropped.y + 1))) {
            dropped = dropped.copy(y = dropped.y + 1)
            distance++
        }
        return copy(active = dropped, score = score + distance * 2).lock(nextInt)
    }

    fun ghost(): FallingPiece {
        var dropped = active
        while (fits(dropped.copy(y = dropped.y + 1))) dropped = dropped.copy(y = dropped.y + 1)
        return dropped
    }

    fun hold(nextInt: (Int) -> Int = Random.Default::nextInt): TetrisGame {
        if (!playable || !canHold) return this
        if (held != null) {
            val incoming = FallingPiece(held)
            return copy(active = incoming, held = active.kind, canHold = false,
                finished = !fits(incoming))
        }
        val (drawn, remainingBag) = draw(bag, nextInt)
        val incoming = FallingPiece(next.first())
        return copy(active = incoming, held = active.kind, canHold = false,
            next = next.drop(1) + drawn, bag = remainingBag, finished = !fits(incoming))
    }

    fun pause() = if (finished) this else copy(paused = true)
    fun resume() = if (finished) this else copy(paused = false)
    fun second() = if (playable) copy(elapsedSeconds = elapsedSeconds + 1) else this

    fun addGarbage(rows: Int, nextInt: (Int) -> Int = Random.Default::nextInt): TetrisGame {
        if (!playable || rows <= 0) return this
        val count = rows.coerceAtMost(TetrisHeight)
        val pushedOut = rows >= TetrisHeight || board.take(count * TetrisWidth).any { it != null }
        val garbage = List(count) {
            val hole = nextInt(TetrisWidth)
            require(hole in 0 until TetrisWidth)
            List(TetrisWidth) { col -> if (col == hole) null else Tetromino.G }
        }.flatten()
        val raised = copy(board = board.drop(count * TetrisWidth) + garbage,
            active = active.copy(y = active.y - count))
        return raised.copy(finished = pushedOut || !raised.fits(raised.active))
    }

    private fun lock(nextInt: (Int) -> Int): TetrisGame {
        if (active.cells.any { it.second < 0 }) return copy(finished = true)
        val placed = board.toMutableList()
        active.cells.forEach { (col, row) -> placed[row * TetrisWidth + col] = active.kind }
        val surviving = (0 until TetrisHeight).map { row ->
            placed.subList(row * TetrisWidth, (row + 1) * TetrisWidth).toList()
        }.filter { row -> row.any { it == null } }
        val cleared = TetrisHeight - surviving.size
        val nextBoard = List(cleared * TetrisWidth) { null } + surviving.flatten()
        val points = listOf(0, 100, 300, 500, 800)[cleared] * level
        val (drawn, remainingBag) = draw(bag, nextInt)
        val incoming = FallingPiece(next.first())
        val nextGame = copy(board = nextBoard, active = incoming, next = next.drop(1) + drawn,
            bag = remainingBag, canHold = true, score = score + points,
            lines = lines + cleared, pieces = pieces + 1)
        return nextGame.copy(finished = !nextGame.fits(incoming))
    }

    companion object {
        fun start(nextInt: (Int) -> Int = Random.Default::nextInt): TetrisGame {
            var bag = emptyList<Tetromino>()
            val pieces = List(4) {
                val (drawn, rest) = draw(bag, nextInt)
                bag = rest
                drawn
            }
            return TetrisGame(List(TetrisCells) { null }, FallingPiece(pieces.first()),
                pieces.drop(1), bag)
        }
    }
}

private fun draw(bag: List<Tetromino>, nextInt: (Int) -> Int): Pair<Tetromino, List<Tetromino>> {
    val available = if (bag.isEmpty()) Tetromino.entries.filter { it != Tetromino.G } else bag
    val index = nextInt(available.size)
    require(index in available.indices)
    return available[index] to available.filterIndexed { position, _ -> position != index }
}

internal fun TetrisGame.serialize(): String = listOf(
    "v1", board.joinToString("") { it?.name ?: "." },
    active.kind.name, active.rotation, active.x, active.y,
    next.joinToString("") { it.name }, bag.joinToString("") { it.name },
    held?.name.orEmpty(), if (canHold) 1 else 0, score, lines, pieces,
    elapsedSeconds, if (paused) 1 else 0, if (finished) 1 else 0,
).joinToString("|")

internal fun restoreTetrisGame(value: String): TetrisGame? = runCatching {
    val fields = value.split('|')
    require(fields.size == 16 && fields[0] == "v1" && fields[1].length == TetrisCells)
    fun kind(char: Char) = Tetromino.valueOf(char.toString())
    fun nonNegative(index: Int) = fields[index].toInt().also { require(it >= 0) }
    fun flag(index: Int) = when (fields[index]) { "1" -> true; "0" -> false; else -> error("Invalid flag") }
    val board = fields[1].map { if (it == '.') null else kind(it) }
    val active = FallingPiece(kind(fields[2].single()), fields[3].toInt(),
        fields[4].toInt(), fields[5].toInt())
    val next = fields[6].map(::kind)
    val bag = fields[7].map(::kind)
    require(active.kind != Tetromino.G && next.size == 3 && next.none { it == Tetromino.G } &&
        bag.none { it == Tetromino.G } && bag.distinct().size == bag.size)
    val held = fields[8].takeIf(String::isNotEmpty)?.single()?.let(::kind)
    require(held != Tetromino.G)
    TetrisGame(board, active, next, bag, held, flag(9), nonNegative(10),
        nonNegative(11), nonNegative(12), nonNegative(13), flag(14), flag(15))
}.getOrNull()
