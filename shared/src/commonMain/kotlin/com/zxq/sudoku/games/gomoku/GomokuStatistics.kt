package com.zxq.sudoku.games.gomoku

internal enum class GomokuResult { BlackWon, WhiteWon, Draw }

internal data class GomokuRecord(
    val mode: GomokuMode,
    val level: GomokuLevel?,
    val startedAt: String,
    val endedAt: String,
    val startedMillis: Long,
    val elapsedSeconds: Int,
    val blackMoves: Int,
    val whiteMoves: Int,
    val result: GomokuResult,
    val finalBoard: String,
) {
    val totalMoves get() = blackMoves + whiteMoves
    val modeLabel get() = if (mode == GomokuMode.Friend) "好友对战" else "人机 · ${level?.label ?: GomokuLevel.Medium.label}"
    val resultLabel get() = when (result) {
        GomokuResult.BlackWon -> "黑方获胜"
        GomokuResult.WhiteWon -> "白方获胜"
        GomokuResult.Draw -> "双方和棋"
    }
}

internal object GomokuBoardCode {
    fun encode(board: List<GomokuStone?>): String {
        require(board.size == GomokuCells)
        return board.joinToString("") { when (it) {
            GomokuStone.Black -> "B"
            GomokuStone.White -> "W"
            null -> "."
        } }
    }

    fun decode(value: String): List<GomokuStone?>? {
        if (value.length != GomokuCells || value.any { it != 'B' && it != 'W' && it != '.' }) return null
        return value.map { when (it) {
            'B' -> GomokuStone.Black
            'W' -> GomokuStone.White
            else -> null
        } }
    }
}

internal data class GomokuStatistics(val records: List<GomokuRecord> = emptyList()) {
    fun completed(game: GomokuGame, startedMillis: Long, startedAt: String,
        endedMillis: Long, endedAt: String): GomokuStatistics {
        if (!game.finished) return this
        val result = when {
            game.drawn -> GomokuResult.Draw
            game.winner == GomokuStone.Black -> GomokuResult.BlackWon
            else -> GomokuResult.WhiteWon
        }
        return copy(records = records + GomokuRecord(
            mode = game.mode, level = game.level, startedAt = startedAt, endedAt = endedAt,
            startedMillis = startedMillis,
            elapsedSeconds = ((endedMillis - startedMillis).coerceAtLeast(0) / 1000).toInt(),
            blackMoves = (game.moves.size + 1) / 2, whiteMoves = game.moves.size / 2,
            result = result, finalBoard = GomokuBoardCode.encode(game.board),
        ))
    }

    fun serialize(): String = "v1|" + records.joinToString(";") { record ->
        listOf(record.mode.name, record.level?.name.orEmpty(), record.startedAt, record.endedAt,
            record.startedMillis, record.elapsedSeconds, record.blackMoves, record.whiteMoves,
            record.result.name, record.finalBoard).joinToString(",")
    }

    companion object {
        fun restore(value: String): GomokuStatistics = runCatching {
            if (value.isEmpty() || value == "v1|") return GomokuStatistics()
            require(value.startsWith("v1|"))
            GomokuStatistics(value.substringAfter('|').split(';').map { row ->
                val fields = row.split(',')
                require(fields.size == 10 && GomokuBoardCode.decode(fields[9]) != null)
                GomokuRecord(
                    mode = GomokuMode.valueOf(fields[0]),
                    level = fields[1].takeIf(String::isNotEmpty)?.let(GomokuLevel::valueOf),
                    startedAt = fields[2], endedAt = fields[3],
                    startedMillis = fields[4].toLong().also { require(it >= 0) },
                    elapsedSeconds = fields[5].toInt().also { require(it >= 0) },
                    blackMoves = fields[6].toInt().also { require(it >= 0) },
                    whiteMoves = fields[7].toInt().also { require(it >= 0) },
                    result = GomokuResult.valueOf(fields[8]), finalBoard = fields[9],
                )
            })
        }.getOrDefault(GomokuStatistics())
    }
}
