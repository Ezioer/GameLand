package com.zxq.sudoku.games.maze

internal data class MazeRecord(
    val played: Int = 0,
    val completed: Int = 0,
    val bestMoves: Int? = null,
    val bestSeconds: Int? = null,
    val bestHints: Int? = null,
    val bestAt: String = "",
)

internal data class MazeStatistics(
    val records: List<MazeRecord> = List(MazeDifficulty.entries.size) { MazeRecord() },
) {
    fun forDifficulty(difficulty: MazeDifficulty) = records[difficulty.ordinal]

    fun started(difficulty: MazeDifficulty): MazeStatistics = update(difficulty) {
        it.copy(played = it.played + 1)
    }

    fun progressed(before: MazeGame, after: MazeGame, at: String): MazeStatistics {
        if (before.completed || !after.completed || before.everCompleted) return this
        return update(after.difficulty) { record ->
            val better = record.bestMoves == null || after.moves < record.bestMoves ||
                (after.moves == record.bestMoves && after.hintsUsed < (record.bestHints ?: Int.MAX_VALUE)) ||
                (after.moves == record.bestMoves && after.hintsUsed == record.bestHints &&
                    after.elapsedSeconds < (record.bestSeconds ?: Int.MAX_VALUE))
            if (better) record.copy(completed = record.completed + 1,
                bestMoves = after.moves, bestSeconds = after.elapsedSeconds,
                bestHints = after.hintsUsed, bestAt = at)
            else record.copy(completed = record.completed + 1)
        }
    }

    private fun update(difficulty: MazeDifficulty, change: (MazeRecord) -> MazeRecord): MazeStatistics =
        copy(records = records.toMutableList().apply {
            this[difficulty.ordinal] = change(this[difficulty.ordinal])
        })

    fun serialize(): String = "v1|" + records.joinToString(";") { record ->
        listOf(record.played, record.completed, record.bestMoves ?: -1,
            record.bestSeconds ?: -1, record.bestHints ?: -1, record.bestAt).joinToString(",")
    }

    companion object {
        fun restore(value: String): MazeStatistics = runCatching {
            require(value.startsWith("v1|"))
            val entries = value.substringAfter('|').split(';')
            require(entries.size == MazeDifficulty.entries.size)
            MazeStatistics(entries.map { entry ->
                val fields = entry.split(',')
                require(fields.size == 6)
                fun count(index: Int) = fields[index].toInt().also { require(it >= 0) }
                fun best(index: Int) = fields[index].toInt().also { require(it >= -1) }.takeIf { it >= 0 }
                MazeRecord(count(0), count(1), best(2), best(3), best(4), fields[5])
            })
        }.getOrDefault(MazeStatistics())
    }
}
