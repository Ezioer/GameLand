package com.zxq.sudoku.games.chess

internal enum class ChessResult { InProgress, RedWon, BlackWon, Left, Draw }

internal data class ChessRecord(
    val mode: ChessMode,
    val level: ChessLevel?,
    val puzzleIndex: Int?,
    val startedAt: String,
    val endedAt: String = "",
    val startedMillis: Long,
    val elapsedSeconds: Int = 0,
    val redMoves: Int = 0,
    val blackMoves: Int = 0,
    val result: ChessResult = ChessResult.InProgress,
    val finalBoard: String,
) {
    val totalMoves get() = redMoves + blackMoves
    val modeLabel get() = when (mode) {
        ChessMode.Human -> "人机 · ${level?.label ?: ChessLevel.Medium.label}"
        ChessMode.Friend -> "好友对战"
        ChessMode.Puzzle -> "残局 · ${ChessPuzzles.entries.getOrNull(puzzleIndex ?: -1)?.title ?: "练习"}"
    }
}

internal object ChessBoardCode {
    private val types = listOf(ChessPieceType.General, ChessPieceType.Advisor, ChessPieceType.Elephant,
        ChessPieceType.Horse, ChessPieceType.Rook, ChessPieceType.Cannon, ChessPieceType.Pawn)
    private val letters = "GAEHRCP"

    fun encode(board: List<ChessPiece?>): String {
        require(board.size == 90)
        return board.joinToString("") { piece ->
            if (piece == null) "." else {
                val symbol = letters[types.indexOf(piece.type)]
                if (piece.side == ChessSide.Red) symbol.toString() else symbol.lowercase()
            }
        }
    }

    fun decode(value: String): List<ChessPiece?>? {
        if (value.length != 90) return null
        return value.map { symbol ->
            if (symbol == '.') null else {
                val typeIndex = letters.indexOf(symbol.uppercaseChar())
                if (typeIndex < 0) return null
                ChessPiece(if (symbol.isUpperCase()) ChessSide.Red else ChessSide.Black, types[typeIndex])
            }
        }
    }
}

internal data class ChessStatistics(val records: List<ChessRecord> = emptyList()) {
    val completedRecords get() = records.filter { it.result == ChessResult.RedWon ||
        it.result == ChessResult.BlackWon || it.result == ChessResult.Draw }

    fun completed(game: ChessGame, startedMillis: Long, startedAt: String, endedMillis: Long, endedAt: String): ChessStatistics {
        if (!game.finished) return this
        val result = when {
            game.drawn -> ChessResult.Draw
            game.winner == ChessSide.Red -> ChessResult.RedWon
            else -> ChessResult.BlackWon
        }
        return copy(records = completedRecords + ChessRecord(
            mode = game.mode, level = game.level, puzzleIndex = game.puzzleIndex,
            startedAt = startedAt, endedAt = endedAt, startedMillis = startedMillis,
            elapsedSeconds = ((endedMillis - startedMillis).coerceAtLeast(0) / 1000).toInt(),
            redMoves = (game.history.size + 1) / 2, blackMoves = game.history.size / 2,
            result = result, finalBoard = ChessBoardCode.encode(game.board),
        ))
    }

    fun serialize(): String = "v1|" + completedRecords.joinToString(";") { record ->
        listOf(
            record.mode.name, record.level?.name.orEmpty(), record.puzzleIndex?.toString().orEmpty(),
            record.startedAt, record.endedAt, record.startedMillis.toString(),
            record.elapsedSeconds.toString(), record.redMoves.toString(), record.blackMoves.toString(),
            record.result.name, record.finalBoard,
        ).joinToString(",")
    }

    companion object {
        fun restore(value: String): ChessStatistics = runCatching {
            if (value.isEmpty()) return ChessStatistics()
            require(value.startsWith("v1|"))
            val body = value.substringAfter('|')
            if (body.isEmpty()) return ChessStatistics()
            ChessStatistics(body.split(';').map { row ->
                val fields = row.split(',')
                require(fields.size == 11)
                val board = fields[10]
                require(ChessBoardCode.decode(board) != null)
                ChessRecord(
                    mode = ChessMode.valueOf(fields[0]),
                    level = fields[1].takeIf(String::isNotEmpty)?.let(ChessLevel::valueOf),
                    puzzleIndex = fields[2].takeIf(String::isNotEmpty)?.toInt(),
                    startedAt = fields[3], endedAt = fields[4],
                    startedMillis = fields[5].toLong().also { require(it >= 0) },
                    elapsedSeconds = fields[6].toInt().also { require(it >= 0) },
                    redMoves = fields[7].toInt().also { require(it >= 0) },
                    blackMoves = fields[8].toInt().also { require(it >= 0) },
                    result = ChessResult.valueOf(fields[9]), finalBoard = board,
                )
            }.filter { it.result == ChessResult.RedWon || it.result == ChessResult.BlackWon || it.result == ChessResult.Draw })
        }.getOrDefault(ChessStatistics())
    }
}
