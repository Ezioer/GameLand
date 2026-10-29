package com.zxq.sudoku.games.klotski

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

private val gameSaver = Saver<MutableState<KlotskiGame?>, String>(
    save = { it.value?.serialize().orEmpty() },
    restore = { mutableStateOf(restoreKlotskiGame(it)) },
)
private val statisticsSaver = Saver<MutableState<KlotskiStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(KlotskiStatistics.restore(it)) },
)

internal class KlotskiSession(
    private val gameState: MutableState<KlotskiGame?>,
    private val statisticsState: MutableState<KlotskiStatistics>,
    private val onGameChange: (String) -> Unit,
    private val onStatisticsChange: (String) -> Unit,
    private val nowLabel: () -> String,
    private val onStart: () -> Unit = {},
) {
    val game get() = gameState.value
    val canContinue get() = gameState.value?.completed == false
    val statistics get() = statisticsState.value

    fun startLevel(index: Int) {
        saveGame(KlotskiGame.start(index))
        onStart()
        saveStatistics(statistics.started(index))
    }

    fun changeGame(next: KlotskiGame) {
        val before = gameState.value ?: return
        if (before == next) return
        saveGame(next)
        saveStatistics(statistics.progressed(before, next, nowLabel()))
    }

    fun tick() {
        gameState.value?.tick()?.let { next ->
            if (next != gameState.value) saveGame(next)
        }
    }

    private fun saveGame(game: KlotskiGame) {
        gameState.value = game
        onGameChange(game.serialize())
    }

    private fun saveStatistics(next: KlotskiStatistics) {
        if (next == statisticsState.value) return
        statisticsState.value = next
        onStatisticsChange(next.serialize())
    }
}

@Composable
internal fun rememberKlotskiSession(initialGame: String, onGameChange: (String) -> Unit,
    initialStatistics: String, onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String, onStart: () -> Unit = {}): KlotskiSession {
    val game = rememberSaveable(saver = gameSaver) {
        mutableStateOf(restoreKlotskiGame(initialGame))
    }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(KlotskiStatistics.restore(initialStatistics))
    }
    return remember(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart) {
        KlotskiSession(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart)
    }
}
