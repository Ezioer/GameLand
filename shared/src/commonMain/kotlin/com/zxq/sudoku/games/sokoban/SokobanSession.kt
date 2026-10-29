package com.zxq.sudoku.games.sokoban

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

private val gameSaver = Saver<MutableState<SokobanGame?>, String>(
    save = { it.value?.serialize().orEmpty() },
    restore = { mutableStateOf(restoreSokobanGame(it)) },
)
private val statisticsSaver = Saver<MutableState<SokobanStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(SokobanStatistics.restore(it)) },
)

internal class SokobanSession(
    private val gameState: MutableState<SokobanGame?>,
    private val statisticsState: MutableState<SokobanStatistics>,
    private val onGameChange: (String) -> Unit,
    private val onStatisticsChange: (String) -> Unit,
    private val nowLabel: () -> String,
    private val onStart: () -> Unit = {},
) {
    val game get() = gameState.value
    val canContinue get() = gameState.value?.completed == false
    val statistics get() = statisticsState.value

    fun startLevel(index: Int) {
        saveGame(SokobanGame.start(index))
        onStart()
        saveStatistics(statistics.started(index))
    }

    fun changeGame(next: SokobanGame) {
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

    private fun saveGame(game: SokobanGame) {
        gameState.value = game
        onGameChange(game.serialize())
    }

    private fun saveStatistics(next: SokobanStatistics) {
        if (next == statisticsState.value) return
        statisticsState.value = next
        onStatisticsChange(next.serialize())
    }
}

@Composable
internal fun rememberSokobanSession(initialGame: String, onGameChange: (String) -> Unit,
    initialStatistics: String, onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String, onStart: () -> Unit = {}): SokobanSession {
    val game = rememberSaveable(saver = gameSaver) {
        mutableStateOf(restoreSokobanGame(initialGame))
    }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(SokobanStatistics.restore(initialStatistics))
    }
    return remember(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart) {
        SokobanSession(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart)
    }
}
