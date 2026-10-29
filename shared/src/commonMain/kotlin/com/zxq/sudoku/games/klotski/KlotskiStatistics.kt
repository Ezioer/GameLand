package com.zxq.sudoku.games.klotski

internal data class KlotskiRecord(
    val played: Int = 0,
    val completed: Int = 0,
    val bestMoves: Int? = null,
    val bestSeconds: Int? = null,
    val bestAt: String = "",
)

internal data class KlotskiStatistics(
    val records: List<KlotskiRecord> = List(KlotskiLevels.size) { KlotskiRecord() },
) {
    fun forLevel(index: Int) = records[index]

    fun started(index: Int): KlotskiStatistics = update(index) { it.copy(played = it.played + 1) }

    fun progressed(before: KlotskiGame, after: KlotskiGame, at: String): KlotskiStatistics {
        if (before.completed || !after.completed) return this
        return update(after.levelIndex) { record ->
            val completed = record.completed + if (before.everCompleted) 0 else 1
            val better = record.bestMoves == null || after.moves < record.bestMoves ||
                (after.moves == record.bestMoves && after.elapsedSeconds <
                    (record.bestSeconds ?: Int.MAX_VALUE))
            if (better) record.copy(completed = completed, bestMoves = after.moves,
                bestSeconds = after.elapsedSeconds, bestAt = at)
            else record.copy(completed = completed)
        }
    }

    private fun update(index: Int, transform: (KlotskiRecord) -> KlotskiRecord): KlotskiStatistics =
        copy(records = records.toMutableList().apply { this[index] = transform(this[index]) })

    fun serialize(): String = "v1|" + records.joinToString(";") { record ->
        listOf(record.played, record.completed, record.bestMoves ?: -1,
            record.bestSeconds ?: -1, record.bestAt).joinToString(",")
    }

    companion object {
        fun restore(value: String): KlotskiStatistics = runCatching {
            require(value.startsWith("v1|"))
            val entries = value.substringAfter('|').split(';')
            require(entries.size == KlotskiLevels.size)
            KlotskiStatistics(entries.map { entry ->
                val fields = entry.split(',')
                require(fields.size == 5)
                fun count(index: Int) = fields[index].toInt().also { require(it >= 0) }
                fun best(index: Int) = fields[index].toInt().also { require(it >= -1) }.takeIf { it >= 0 }
                KlotskiRecord(count(0), count(1), best(2), best(3), fields[4])
            })
        }.getOrDefault(KlotskiStatistics())
    }
}
