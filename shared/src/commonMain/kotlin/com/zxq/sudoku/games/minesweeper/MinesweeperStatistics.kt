package com.zxq.sudoku.games.minesweeper

internal data class MinesweeperRecord(
    val played: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val bestSeconds: Int? = null,
    val bestAt: String = "",
)

internal data class MinesweeperStatistics(
    val records: List<MinesweeperRecord> = List(MinesweeperDifficulty.entries.size) { MinesweeperRecord() },
) {
    fun forDifficulty(difficulty: MinesweeperDifficulty) = records[difficulty.ordinal]

    fun started(difficulty: MinesweeperDifficulty) = update(difficulty) { record ->
        record.copy(played = record.played + 1)
    }

    fun progressed(before: MinesweeperGame, after: MinesweeperGame, at: String): MinesweeperStatistics {
        if (before.status != MinesweeperStatus.Playing || after.status == MinesweeperStatus.Playing) return this
        return update(after.difficulty) { record ->
            when (after.status) {
                MinesweeperStatus.Won -> {
                    val better = record.bestSeconds == null || after.elapsedSeconds < record.bestSeconds
                    record.copy(won = record.won + 1,
                        bestSeconds = if (better) after.elapsedSeconds else record.bestSeconds,
                        bestAt = if (better) at else record.bestAt)
                }
                MinesweeperStatus.Lost -> record.copy(lost = record.lost + 1)
                MinesweeperStatus.Playing -> record
            }
        }
    }

    private fun update(difficulty: MinesweeperDifficulty,
        change: (MinesweeperRecord) -> MinesweeperRecord): MinesweeperStatistics =
        copy(records = records.toMutableList().apply {
            this[difficulty.ordinal] = change(this[difficulty.ordinal])
        })

    fun serialize(): String = "v1|" + records.joinToString(";") { record ->
        listOf(record.played, record.won, record.lost,
            record.bestSeconds ?: -1, record.bestAt).joinToString(",")
    }

    companion object {
        fun restore(value: String): MinesweeperStatistics = runCatching {
            require(value.startsWith("v1|"))
            val records = value.substringAfter('|').split(';')
            require(records.size == MinesweeperDifficulty.entries.size)
            MinesweeperStatistics(records.map { entry ->
                val fields = entry.split(',')
                require(fields.size == 5)
                fun count(index: Int) = fields[index].toInt().also { require(it >= 0) }
                val best = fields[3].toInt().also { require(it >= -1) }.takeIf { it >= 0 }
                MinesweeperRecord(count(0), count(1), count(2), best, fields[4])
            })
        }.getOrDefault(MinesweeperStatistics())
    }
}
