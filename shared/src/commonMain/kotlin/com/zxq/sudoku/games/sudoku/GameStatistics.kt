package com.zxq.sudoku.games.sudoku

data class DifficultyStatistics(
    val played: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
)

enum class GameResult { InProgress, Left, Won, Lost }

data class GameRecord(
    val date: String,
    val elapsedSeconds: Int = 0,
    val mistakes: Int = 0,
    val result: GameResult = GameResult.InProgress,
)

data class GameStatistics(
    private val records: Map<SudokuDifficulty, DifficultyStatistics> = emptyMap(),
    private val games: Map<SudokuDifficulty, List<GameRecord>> = emptyMap(),
) {
    fun forDifficulty(difficulty: SudokuDifficulty): DifficultyStatistics =
        records[difficulty] ?: DifficultyStatistics()

    fun gamesFor(difficulty: SudokuDifficulty): List<GameRecord> = games[difficulty].orEmpty()

    fun started(difficulty: SudokuDifficulty, date: String = ""): GameStatistics =
        update(difficulty) { it.copy(played = it.played + 1) }
            .copy(games = games + (difficulty to (gamesFor(difficulty) + GameRecord(date))))

    fun afterGameChange(previous: SudokuGame, current: SudokuGame): GameStatistics {
        if (previous.puzzle != current.puzzle) return this
        val difficulty = current.puzzle.difficulty
        val history = gamesFor(difficulty)
        val updatedHistory = if (history.isEmpty()) games else games + (difficulty to history.dropLast(1) + history.last().copy(
            elapsedSeconds = current.elapsedSeconds,
            mistakes = current.mistakes,
            result = when {
                current.won -> GameResult.Won
                current.finished -> GameResult.Lost
                else -> history.last().result
            },
        ))
        val updated = copy(games = updatedHistory)
        if (previous.finished || !current.finished) return updated
        return updated.update(difficulty) {
            if (current.won) it.copy(wins = it.wins + 1) else it.copy(losses = it.losses + 1)
        }
    }

    fun left(game: SudokuGame): GameStatistics {
        val difficulty = game.puzzle.difficulty
        val history = gamesFor(difficulty)
        if (history.isEmpty() || history.last().result != GameResult.InProgress) return this
        return copy(games = games + (difficulty to history.dropLast(1) + history.last().copy(
            elapsedSeconds = game.elapsedSeconds,
            mistakes = game.mistakes,
            result = GameResult.Left,
        )))
    }

    fun serialize(): String = SudokuDifficulty.entries.joinToString(";") { difficulty ->
        val record = forDifficulty(difficulty)
        "${record.played},${record.wins},${record.losses}"
    } + "|" + SudokuDifficulty.entries.flatMapIndexed { index, difficulty ->
        gamesFor(difficulty).map { game ->
            "$index,${game.date},${game.elapsedSeconds},${game.mistakes},${game.result.ordinal}"
        }
    }.joinToString(";")

    private fun update(
        difficulty: SudokuDifficulty,
        transform: (DifficultyStatistics) -> DifficultyStatistics,
    ): GameStatistics = copy(records = records + (difficulty to transform(forDifficulty(difficulty))))

    companion object {
        fun restore(value: String): GameStatistics = runCatching {
            val sections = value.split('|')
            require(sections.size in 1..2)
            val parts = sections[0].split(';')
            require(parts.size == SudokuDifficulty.entries.size)
            val records = SudokuDifficulty.entries.zip(parts).associate { (difficulty, part) ->
                val numbers = part.split(',').map(String::toInt)
                require(numbers.size == 3 && numbers.all { it >= 0 })
                val record = DifficultyStatistics(numbers[0], numbers[1], numbers[2])
                require(record.wins + record.losses <= record.played)
                difficulty to record
            }.filterValues { it != DifficultyStatistics() }
            val games = sections.getOrNull(1).orEmpty().takeIf { it.isNotEmpty() }
                ?.split(';')?.map { part ->
                    val fields = part.split(',')
                    require(fields.size == 5)
                    val difficulty = SudokuDifficulty.entries[fields[0].toInt()]
                    val date = fields[1]
                    require(date.isEmpty() || (date.length == 10 && date[4] == '-' && date[7] == '-' && date.filterIndexed { index, _ -> index != 4 && index != 7 }.all(Char::isDigit)))
                    val seconds = fields[2].toInt().also { require(it >= 0) }
                    val mistakes = fields[3].toInt().also { require(it in 0..3) }
                    val result = GameResult.entries[fields[4].toInt()]
                    difficulty to GameRecord(date, seconds, mistakes, result)
                }?.groupBy({ it.first }, { it.second }).orEmpty()
            require(SudokuDifficulty.entries.all { gamesForDifficulty -> games[gamesForDifficulty].orEmpty().size <= (records[gamesForDifficulty]?.played ?: 0) })
            GameStatistics(records, games)
        }.getOrDefault(GameStatistics())
    }
}
