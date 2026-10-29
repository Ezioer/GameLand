package com.zxq.sudoku.games.internationalchess

internal data class InternationalChessRecord(
    val mode: ChessMode,
    val level: ChessLevel?,
    val startedAt: String,
    val endedAt: String,
    val startedMillis: Long,
    val elapsedSeconds: Int,
    val whiteMoves: Int,
    val blackMoves: Int,
    val result: String,
    val finalBoard: String,
) {
    val modeLabel get() = if (mode == ChessMode.Friend) "好友对战" else "人机 · ${level?.label ?: "简单"}"
    val totalMoves get() = whiteMoves + blackMoves
}

internal object InternationalChessBoardCode {
    fun encode(board: List<Piece?>): String = board.joinToString("") { piece ->
        if (piece == null) "." else {
            val letter = when (piece.kind) {
                PieceKind.King -> 'k'; PieceKind.Queen -> 'q'; PieceKind.Rook -> 'r'
                PieceKind.Bishop -> 'b'; PieceKind.Knight -> 'n'; PieceKind.Pawn -> 'p'
            }
            if (piece.color == ChessColor.White) letter.uppercaseChar().toString() else letter.toString()
        }
    }

    fun decode(value: String): List<Piece?>? {
        if (value.length != 64) return null
        return value.map { char ->
            if (char == '.') null else {
                val kind = when (char.lowercaseChar()) {
                    'k' -> PieceKind.King; 'q' -> PieceKind.Queen; 'r' -> PieceKind.Rook
                    'b' -> PieceKind.Bishop; 'n' -> PieceKind.Knight; 'p' -> PieceKind.Pawn
                    else -> return null
                }
                Piece(if (char.isUpperCase()) ChessColor.White else ChessColor.Black, kind)
            }
        }
    }
}

internal data class InternationalChessStatistics(val records: List<InternationalChessRecord> = emptyList()) {
    fun completed(game: ChessGame, startedMillis: Long, startedAt: String,
        endedMillis: Long, endedAt: String): InternationalChessStatistics {
        if (!game.finished) return this
        val result = when (game.winner) {
            ChessColor.White -> "白方获胜"
            ChessColor.Black -> "黑方获胜"
            null -> "和棋 · ${game.drawReason}"
        }
        return copy(records = records + InternationalChessRecord(
            game.mode, game.level, startedAt, endedAt, startedMillis,
            ((endedMillis - startedMillis).coerceAtLeast(0) / 1000).toInt(),
            (game.moves.size + 1) / 2, game.moves.size / 2, result,
            InternationalChessBoardCode.encode(game.position.board),
        ))
    }

    fun serialize(): String = "v1|" + records.joinToString(";") { record ->
        listOf(record.mode.name, record.level?.name.orEmpty(), record.startedAt, record.endedAt,
            record.startedMillis, record.elapsedSeconds, record.whiteMoves, record.blackMoves,
            record.result, record.finalBoard).joinToString(",")
    }

    companion object {
        fun restore(value: String): InternationalChessStatistics = runCatching {
            if (value.isEmpty() || value == "v1|") return InternationalChessStatistics()
            require(value.startsWith("v1|"))
            InternationalChessStatistics(value.substringAfter('|').split(';').map { row ->
                val fields = row.split(',')
                require(fields.size == 10 && InternationalChessBoardCode.decode(fields[9]) != null)
                InternationalChessRecord(
                    ChessMode.valueOf(fields[0]),
                    fields[1].takeIf(String::isNotEmpty)?.let(ChessLevel::valueOf),
                    fields[2], fields[3], fields[4].toLong().also { require(it >= 0) },
                    fields[5].toInt().also { require(it >= 0) },
                    fields[6].toInt().also { require(it >= 0) },
                    fields[7].toInt().also { require(it >= 0) }, fields[8], fields[9],
                )
            })
        }.getOrDefault(InternationalChessStatistics())
    }
}

internal fun chessDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = seconds % 3600 / 60
    val remainder = seconds % 60
    return if (hours > 0) "${hours}时${minutes}分${remainder}秒" else "${minutes}分${remainder}秒"
}
