package com.zxq.sudoku.games.tetris

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

private val statisticsSaver = Saver<MutableState<TetrisStatistics>, String>(
    save = { it.value.serialize() }, restore = { mutableStateOf(TetrisStatistics.restore(it)) })
private val gameSaver = Saver<MutableState<TetrisGame?>, String>(
    save = { it.value?.serialize().orEmpty() }, restore = { mutableStateOf(restoreTetrisGame(it)) })

internal class TetrisSession(
    private val gameState: MutableState<TetrisGame?>,
    private val statisticsState: MutableState<TetrisStatistics>,
    private val onGameChange: (String) -> Unit,
    private val onStatisticsChange: (String) -> Unit,
    private val nowLabel: () -> String,
    private val onStart: () -> Unit = {},
) {
    val game get() = gameState.value
    val canContinue get() = gameState.value?.finished == false
    val statistics get() = statisticsState.value

    fun startGame() {
        saveGame(TetrisGame.start())
        onStart()
        updateStatistics(statistics.started())
    }

    fun resumeGame() {
        gameState.value?.let { changeGame(it.resume()) }
    }

    fun changeGame(next: TetrisGame) {
        val before = gameState.value ?: return
        if (before == next) return
        saveGame(next)
        val updated = statistics.progressed(before, next, nowLabel())
        if (updated != statistics) updateStatistics(updated)
    }

    private fun saveGame(next: TetrisGame) {
        gameState.value = next
        onGameChange(next.serialize())
    }

    private fun updateStatistics(next: TetrisStatistics) {
        statisticsState.value = next
        onStatisticsChange(next.serialize())
    }
}

@Composable
internal fun rememberTetrisSession(
    initialGame: String, onGameChange: (String) -> Unit,
    initialStatistics: String, onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String,
    onStart: () -> Unit = {},
): TetrisSession {
    val game = rememberSaveable(saver = gameSaver) { mutableStateOf(restoreTetrisGame(initialGame)) }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(TetrisStatistics.restore(initialStatistics))
    }
    return remember(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart) {
        TetrisSession(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart)
    }
}
