package com.zxq.sudoku.games.game2048

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

private val statisticsSaver = Saver<MutableState<Game2048Statistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(Game2048Statistics.restore(it)) },
)

private val gameSaver = Saver<MutableState<Game2048Game?>, String>(
    save = { it.value?.serialize().orEmpty() },
    restore = { mutableStateOf(restoreGame2048(it)) },
)

internal class Game2048Session(
    private val gameState: MutableState<Game2048Game?>,
    private val statisticsState: MutableState<Game2048Statistics>,
    private val onStatisticsChange: (String) -> Unit,
    private val onGameChange: (String) -> Unit,
    private val nowLabel: () -> String,
    private val onStart: () -> Unit = {},
) {
    val game get() = gameState.value
    val canContinue get() = gameState.value?.finished == false
    val statistics get() = statisticsState.value

    fun startGame() {
        saveGame(Game2048Game.start())
        onStart()
        updateStatistics(statistics.started())
    }

    fun changeGame(next: Game2048Game) {
        val before = gameState.value ?: return
        if (before == next) return
        saveGame(next)
        updateStatistics(statistics.progressed(before, next, nowLabel()))
    }

    private fun saveGame(next: Game2048Game) {
        gameState.value = next
        onGameChange(next.serialize())
    }

    private fun updateStatistics(next: Game2048Statistics) {
        if (next == statistics) return
        statisticsState.value = next
        onStatisticsChange(next.serialize())
    }
}

@Composable
internal fun rememberGame2048Session(initialStatistics: String,
    onStatisticsChange: (String) -> Unit, initialGame: String,
    onGameChange: (String) -> Unit, nowLabel: () -> String,
    onStart: () -> Unit = {}): Game2048Session {
    val game = rememberSaveable(saver = gameSaver) {
        mutableStateOf(restoreGame2048(initialGame))
    }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(Game2048Statistics.restore(initialStatistics))
    }
    return remember(game, statistics, onStatisticsChange, onGameChange, nowLabel, onStart) {
        Game2048Session(game, statistics, onStatisticsChange, onGameChange, nowLabel, onStart)
    }
}
