package com.zxq.sudoku.games.go

internal enum class GoResult { BlackWon, WhiteWon, Draw }

internal data class GoRecord(
    val mode: GoMode,
    val level: GoLevel?,
    val startedAt: String,
    val endedAt: String,
    val startedMillis: Long,
    val elapsedSeconds: Int,
    val totalMoves: Int,
    val blackCaptures: Int,
    val whiteCaptures: Int,
    val result: GoResult,
    val ending: GoEnding,
    val blackArea: Int?,
    val whiteArea: Int?,
    val finalBoard: String,
) {
    val modeLabel get() = if (mode == GoMode.Friend) "好友对战" else "人机 · ${level?.label ?: GoLevel.Medium.label}"
    val resultLabel get() = when (result) {
        GoResult.BlackWon -> "黑方获胜"
        GoResult.WhiteWon -> "白方获胜"
        GoResult.Draw -> "双方和棋"
    }
    val endingLabel get() = when (ending) {
        GoEnding.Counted -> "数子终局"
        GoEnding.Resigned -> "认输"
        GoEnding.AgreedDraw -> "协商和棋"
    }
}

internal object GoBoardCode {
    fun encode(board: List<GoStone?>): String {
        require(board.size == GoCells)
        return board.joinToString("") { when (it) {
            GoStone.Black -> "B"
            GoStone.White -> "W"
            null -> "."
        } }
    }

    fun decode(value: String): List<GoStone?>? {
        if (value.length != GoCells || value.any { it != 'B' && it != 'W' && it != '.' }) return null
        return value.map { when (it) {
            'B' -> GoStone.Black
            'W' -> GoStone.White
            else -> null
        } }
    }
}

internal data class GoStatistics(val records: List<GoRecord> = emptyList()) {
    fun completed(game: GoGame, startedMillis: Long, startedAt: String,
        endedMillis: Long, endedAt: String): GoStatistics {
        if (!game.finished) return this
        val result = when {
            game.drawn -> GoResult.Draw
            game.winner == GoStone.Black -> GoResult.BlackWon
            else -> GoResult.WhiteWon
        }
        return copy(records = records + GoRecord(
            mode = game.mode, level = game.level, startedAt = startedAt, endedAt = endedAt,
            startedMillis = startedMillis,
            elapsedSeconds = ((endedMillis - startedMillis).coerceAtLeast(0) / 1000).toInt(),
            totalMoves = game.moveCount, blackCaptures = game.blackCaptures,
            whiteCaptures = game.whiteCaptures, result = result,
            ending = game.ending ?: GoEnding.Counted,
            blackArea = game.score?.blackArea, whiteArea = game.score?.whiteArea,
            finalBoard = GoBoardCode.encode(game.effectiveBoard),
        ))
    }

    fun serialize(): String = "v1|" + records.joinToString(";") { record ->
        listOf(record.mode.name, record.level?.name.orEmpty(), record.startedAt, record.endedAt,
            record.startedMillis, record.elapsedSeconds, record.totalMoves, record.blackCaptures,
            record.whiteCaptures, record.result.name, record.ending.name,
            record.blackArea?.toString().orEmpty(), record.whiteArea?.toString().orEmpty(),
            record.finalBoard).joinToString(",")
    }

    companion object {
        fun restore(value: String): GoStatistics = runCatching {
            if (value.isEmpty() || value == "v1|") return GoStatistics()
            require(value.startsWith("v1|"))
            GoStatistics(value.substringAfter('|').split(';').map { row ->
                val fields = row.split(',')
                require(fields.size == 14 && GoBoardCode.decode(fields[13]) != null)
                GoRecord(
                    mode = GoMode.valueOf(fields[0]),
                    level = fields[1].takeIf(String::isNotEmpty)?.let(GoLevel::valueOf),
                    startedAt = fields[2], endedAt = fields[3],
                    startedMillis = fields[4].toLong().also { require(it >= 0) },
                    elapsedSeconds = fields[5].toInt().also { require(it >= 0) },
                    totalMoves = fields[6].toInt().also { require(it >= 0) },
                    blackCaptures = fields[7].toInt().also { require(it >= 0) },
                    whiteCaptures = fields[8].toInt().also { require(it >= 0) },
                    result = GoResult.valueOf(fields[9]), ending = GoEnding.valueOf(fields[10]),
                    blackArea = fields[11].takeIf(String::isNotEmpty)?.toInt(),
                    whiteArea = fields[12].takeIf(String::isNotEmpty)?.toInt(),
                    finalBoard = fields[13],
                )
            })
        }.getOrDefault(GoStatistics())
    }
}
