package com.zxq.sudoku.games.game2048

internal data class Game2048Statistics(
    val bestScore: Int = 0,
    val bestTile: Int = 0,
    val bestAt: String = "",
    val played: Int = 0,
    val completed: Int = 0,
    val wins: Int = 0,
) {
    fun started() = copy(played = played + 1)

    fun progressed(before: Game2048Game, after: Game2048Game, at: String): Game2048Statistics {
        if (before == after) return this
        val newRecord = after.score > bestScore
        return copy(
            bestScore = maxOf(bestScore, after.score),
            bestTile = maxOf(bestTile, after.maxTile),
            bestAt = if (newRecord) at else bestAt,
            completed = completed + if (!before.everFinished && after.everFinished) 1 else 0,
            wins = wins + if (!before.everWon && after.everWon) 1 else 0,
        )
    }

    fun serialize(): String = "v1|$bestScore,$bestTile,$bestAt,$played,$completed,$wins"

    companion object {
        fun restore(value: String): Game2048Statistics = runCatching {
            if (value.isEmpty()) return Game2048Statistics()
            require(value.startsWith("v1|"))
            val fields = value.substringAfter('|').split(',')
            require(fields.size == 6)
            Game2048Statistics(
                bestScore = fields[0].toInt().also { require(it >= 0) },
                bestTile = fields[1].toInt().also { require(it >= 0) },
                bestAt = fields[2],
                played = fields[3].toInt().also { require(it >= 0) },
                completed = fields[4].toInt().also { require(it >= 0) },
                wins = fields[5].toInt().also { require(it >= 0) },
            )
        }.getOrDefault(Game2048Statistics())
    }
}
