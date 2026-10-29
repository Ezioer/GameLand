package com.zxq.sudoku.games.maze

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

private val gameSaver = Saver<MutableState<MazeGame?>, String>(
    save = { it.value?.serialize().orEmpty() },
    restore = { mutableStateOf(restoreMazeGame(it)) },
)
private val statisticsSaver = Saver<MutableState<MazeStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(MazeStatistics.restore(it)) },
)

internal class MazeSession(
    private val gameState: MutableState<MazeGame?>,
    private val statisticsState: MutableState<MazeStatistics>,
    private val onGameChange: (String) -> Unit,
    private val onStatisticsChange: (String) -> Unit,
    private val nowLabel: () -> String,
    private val onStart: () -> Unit = {},
) {
    val game get() = gameState.value
    val canContinue get() = gameState.value?.completed == false
    val statistics get() = statisticsState.value

    fun startGame(difficulty: MazeDifficulty) {
        saveGame(MazeGame.start(difficulty))
        onStart()
        saveStatistics(statistics.started(difficulty))
    }

    fun restartGame() {
        val current = gameState.value ?: return
        saveGame(current.restart())
        onStart()
        saveStatistics(statistics.started(current.difficulty))
    }

    fun changeGame(next: MazeGame) {
        val before = gameState.value ?: return
        if (next == before) return
        saveGame(next)
        saveStatistics(statistics.progressed(before, next, nowLabel()))
    }

    fun tick() {
        gameState.value?.tick()?.let { next ->
            if (next != gameState.value) saveGame(next)
        }
    }

    private fun saveGame(game: MazeGame) {
        gameState.value = game
        onGameChange(game.serialize())
    }

    private fun saveStatistics(next: MazeStatistics) {
        if (next == statisticsState.value) return
        statisticsState.value = next
        onStatisticsChange(next.serialize())
    }
}

@Composable
internal fun rememberMazeSession(
    initialGame: String, onGameChange: (String) -> Unit,
    initialStatistics: String, onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String,
    onStart: () -> Unit = {},
): MazeSession {
    val game = rememberSaveable(saver = gameSaver) {
        mutableStateOf(restoreMazeGame(initialGame))
    }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(MazeStatistics.restore(initialStatistics))
    }
    return remember(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart) {
        MazeSession(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart)
    }
}
