package com.zxq.sudoku.games.tetris

internal data class TetrisStatistics(
    val bestScore: Int = 0,
    val bestLines: Int = 0,
    val bestLevel: Int = 0,
    val bestAt: String = "",
    val played: Int = 0,
    val finished: Int = 0,
    val totalLines: Int = 0,
) {
    fun started() = copy(played = played + 1)

    fun progressed(before: TetrisGame, after: TetrisGame, at: String): TetrisStatistics {
        if (before == after) return this
        return copy(
            bestScore = maxOf(bestScore, after.score),
            bestLines = maxOf(bestLines, after.lines),
            bestLevel = maxOf(bestLevel, after.level),
            bestAt = if (after.score > bestScore) at else bestAt,
            finished = finished + if (!before.finished && after.finished) 1 else 0,
            totalLines = totalLines + (after.lines - before.lines).coerceAtLeast(0),
        )
    }

    fun serialize() = listOf("v1", bestScore, bestLines, bestLevel, bestAt,
        played, finished, totalLines).joinToString("|")

    companion object {
        fun restore(value: String): TetrisStatistics = runCatching {
            if (value.isEmpty()) return TetrisStatistics()
            val fields = value.split('|')
            require(fields.size == 8 && fields[0] == "v1")
            fun number(index: Int) = fields[index].toInt().also { require(it >= 0) }
            TetrisStatistics(number(1), number(2), number(3), fields[4],
                number(5), number(6), number(7))
        }.getOrDefault(TetrisStatistics())
    }
}
