package com.zxq.sudoku.games.sokoban

internal data class SokobanRecord(
    val played: Int = 0,
    val completed: Int = 0,
    val bestMoves: Int? = null,
    val bestPushes: Int? = null,
    val bestSeconds: Int? = null,
    val bestAt: String = "",
)

internal data class SokobanStatistics(
    val records: List<SokobanRecord> = List(SokobanLevels.size) { SokobanRecord() },
) {
    fun forLevel(index: Int) = records[index]

    fun started(index: Int): SokobanStatistics = update(index) { it.copy(played = it.played + 1) }

    fun progressed(before: SokobanGame, after: SokobanGame, at: String): SokobanStatistics {
        if (before.completed || !after.completed) return this
        return update(after.levelIndex) { record ->
            val addedCompletion = if (before.everCompleted) 0 else 1
            val better = record.bestMoves == null || after.moves < record.bestMoves ||
                (after.moves == record.bestMoves && after.pushes < (record.bestPushes ?: Int.MAX_VALUE)) ||
                (after.moves == record.bestMoves && after.pushes == record.bestPushes &&
                    after.elapsedSeconds < (record.bestSeconds ?: Int.MAX_VALUE))
            if (better) record.copy(completed = record.completed + addedCompletion,
                bestMoves = after.moves, bestPushes = after.pushes,
                bestSeconds = after.elapsedSeconds, bestAt = at)
            else record.copy(completed = record.completed + addedCompletion)
        }
    }

    private fun update(index: Int, transform: (SokobanRecord) -> SokobanRecord): SokobanStatistics =
        copy(records = records.toMutableList().apply { this[index] = transform(this[index]) })

    fun serialize(): String = "v1|" + records.joinToString(";") { record ->
        listOf(record.played, record.completed, record.bestMoves ?: -1,
            record.bestPushes ?: -1, record.bestSeconds ?: -1, record.bestAt).joinToString(",")
    }

    companion object {
        fun restore(value: String): SokobanStatistics = runCatching {
            require(value.startsWith("v1|"))
            val entries = value.substringAfter('|').split(';')
            require(entries.size == SokobanLevels.size)
            SokobanStatistics(entries.map { entry ->
                val fields = entry.split(',')
                require(fields.size == 6)
                fun count(index: Int) = fields[index].toInt().also { require(it >= 0) }
                fun best(index: Int) = fields[index].toInt().also { require(it >= -1) }.takeIf { it >= 0 }
                SokobanRecord(count(0), count(1), best(2), best(3), best(4), fields[5])
            })
        }.getOrDefault(SokobanStatistics())
    }
}
