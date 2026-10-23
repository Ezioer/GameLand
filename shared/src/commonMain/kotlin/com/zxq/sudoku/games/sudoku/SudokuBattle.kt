package com.zxq.sudoku.games.sudoku

data class SudokuBattle(
    val first: SudokuGame,
    val second: SudokuGame,
    val elapsedSeconds: Int = 0,
    val paused: Boolean = false,
) {
    val winner: Int? get() = when {
        first.won -> 1
        second.won -> 2
        else -> null
    }
    val finished: Boolean get() = winner != null || (first.finished && second.finished)

    fun updatePlayer(player: Int, change: (SudokuGame) -> SudokuGame): SudokuBattle {
        if (paused || finished) return this
        return when (player) {
            1 -> if (first.finished) this else copy(first = change(first))
            2 -> if (second.finished) this else copy(second = change(second))
            else -> this
        }
    }

    fun togglePause(): SudokuBattle = if (finished) this else copy(paused = !paused)

    fun tick(): SudokuBattle = if (paused || finished) this else copy(elapsedSeconds = elapsedSeconds + 1)

    fun serialize(): String = listOf(
        first.serialize(), second.serialize(), elapsedSeconds.toString(), paused.toString(),
    ).joinToString("\n")

    companion object {
        fun start(puzzle: SudokuPuzzle): SudokuBattle =
            SudokuBattle(SudokuGame.start(puzzle), SudokuGame.start(puzzle))

        fun restore(saved: String): SudokuBattle? = runCatching {
            val parts = saved.split('\n')
            require(parts.size == 4)
            val first = requireNotNull(SudokuGame.restore(parts[0]))
            val second = requireNotNull(SudokuGame.restore(parts[1]))
            require(first.puzzle == second.puzzle)
            val elapsedSeconds = parts[2].toInt().also { require(it >= 0) }
            SudokuBattle(first, second, elapsedSeconds, parts[3].toBooleanStrict())
        }.getOrNull()
    }
}
