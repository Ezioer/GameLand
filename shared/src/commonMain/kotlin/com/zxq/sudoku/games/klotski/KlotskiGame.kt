package com.zxq.sudoku.games.klotski

internal const val KlotskiColumns = 4
internal const val KlotskiRows = 5

internal enum class KlotskiDirection(val dx: Int, val dy: Int) {
    Up(0, -1), Down(0, 1), Left(-1, 0), Right(1, 0),
}

internal data class KlotskiPiece(val name: String, val width: Int, val height: Int)

internal val KlotskiPieces = listOf(
    KlotskiPiece("曹操", 2, 2),
    KlotskiPiece("张飞", 1, 2), KlotskiPiece("赵云", 1, 2),
    KlotskiPiece("马超", 1, 2), KlotskiPiece("黄忠", 1, 2),
    KlotskiPiece("关羽", 2, 1),
    KlotskiPiece("兵", 1, 1), KlotskiPiece("兵", 1, 1),
    KlotskiPiece("兵", 1, 1), KlotskiPiece("兵", 1, 1),
)

internal data class KlotskiLevel(val title: String, val difficulty: String,
    val positions: List<Int>)

internal val KlotskiLevels = listOf(
    KlotskiLevel("近在眼前", "入门", listOf(12, 3, 2, 1, 0, 10, 8, 15, 19, 9)),
    KlotskiLevel("让出道路", "简单", listOf(12, 3, 2, 1, 0, 14, 10, 18, 19, 11)),
    KlotskiLevel("层层挪移", "中级", listOf(9, 3, 2, 0, 8, 18, 15, 16, 17, 11)),
    KlotskiLevel("横刀立马", "经典", listOf(1, 0, 3, 8, 11, 9, 13, 14, 16, 19)),
)

private fun occupiedCells(topLeft: Int, piece: KlotskiPiece): Set<Int> {
    val x = topLeft % KlotskiColumns
    val y = topLeft / KlotskiColumns
    if (x + piece.width > KlotskiColumns || y + piece.height > KlotskiRows) return emptySet()
    return buildSet {
        for (row in y until y + piece.height)
            for (column in x until x + piece.width)
                add(row * KlotskiColumns + column)
    }
}

internal fun validKlotskiPositions(positions: List<Int>): Boolean {
    if (positions.size != KlotskiPieces.size) return false
    val occupied = mutableSetOf<Int>()
    return positions.indices.all { index ->
        val topLeft = positions[index]
        if (topLeft !in 0 until KlotskiColumns * KlotskiRows) return@all false
        val cells = occupiedCells(topLeft, KlotskiPieces[index])
        if (cells.isEmpty() || cells.any { it in occupied }) false
        else { occupied.addAll(cells); true }
    }
}

internal data class KlotskiGame(
    val levelIndex: Int,
    val positions: List<Int>,
    val moves: Int = 0,
    val elapsedSeconds: Int = 0,
    val history: List<List<Int>> = emptyList(),
    val everCompleted: Boolean = false,
) {
    val completed get() = positions[0] == 13

    fun move(pieceIndex: Int, direction: KlotskiDirection, steps: Int = 1): KlotskiGame {
        if (completed || pieceIndex !in KlotskiPieces.indices || steps <= 0) return this
        val piece = KlotskiPieces[pieceIndex]
        val occupiedByOthers = positions.indices.filter { it != pieceIndex }
            .flatMap { occupiedCells(positions[it], KlotskiPieces[it]) }.toSet()
        var topLeft = positions[pieceIndex]
        for (step in 0 until steps) {
            val x = topLeft % KlotskiColumns + direction.dx
            val y = topLeft / KlotskiColumns + direction.dy
            if (x !in 0..KlotskiColumns - piece.width ||
                y !in 0..KlotskiRows - piece.height) break
            val candidate = y * KlotskiColumns + x
            if (occupiedCells(candidate, piece).any { it in occupiedByOthers }) break
            topLeft = candidate
        }
        if (topLeft == positions[pieceIndex]) return this
        val next = positions.toMutableList().apply { this[pieceIndex] = topLeft }
        return copy(positions = next, moves = moves + 1, history = history + listOf(positions),
            everCompleted = everCompleted || next[0] == 13)
    }

    fun undo(): KlotskiGame {
        val previous = history.lastOrNull() ?: return this
        return copy(positions = previous, moves = moves - 1, history = history.dropLast(1))
    }

    fun tick(): KlotskiGame = if (completed) this else copy(elapsedSeconds = elapsedSeconds + 1)

    companion object {
        fun start(levelIndex: Int): KlotskiGame =
            KlotskiGame(levelIndex, KlotskiLevels[levelIndex].positions)
    }
}

internal fun KlotskiGame.serialize(): String = buildString {
    append("v1|").append(levelIndex).append('|')
    append(positions.joinToString(",")).append('|')
    append(moves).append('|').append(elapsedSeconds).append('|')
    append(if (everCompleted) 1 else 0).append('|')
    append(history.joinToString(";") { it.joinToString(",") })
}

internal fun restoreKlotskiGame(value: String): KlotskiGame? = runCatching {
    val fields = value.split('|')
    require(fields.size == 7 && fields[0] == "v1")
    val levelIndex = fields[1].toInt()
    require(levelIndex in KlotskiLevels.indices)
    fun positions(text: String) = text.split(',').map { it.toInt() }
        .also { require(validKlotskiPositions(it)) }
    fun count(text: String) = text.toInt().also { require(it >= 0) }
    require(fields[5] == "0" || fields[5] == "1")
    val history = if (fields[6].isEmpty()) emptyList() else fields[6].split(';').map(::positions)
    KlotskiGame(levelIndex, positions(fields[2]), count(fields[3]), count(fields[4]),
        history, fields[5] == "1").also { require(it.moves == it.history.size) }
}.getOrNull()
