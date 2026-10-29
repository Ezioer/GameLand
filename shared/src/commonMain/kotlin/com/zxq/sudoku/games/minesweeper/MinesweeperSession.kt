package com.zxq.sudoku.games.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

private val gameSaver = Saver<MutableState<MinesweeperGame?>, String>(
    save = { it.value?.serialize().orEmpty() },
    restore = { mutableStateOf(restoreMinesweeperGame(it)) },
)
private val statisticsSaver = Saver<MutableState<MinesweeperStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(MinesweeperStatistics.restore(it)) },
)

internal class MinesweeperSession(
    private val gameState: MutableState<MinesweeperGame?>,
    private val statisticsState: MutableState<MinesweeperStatistics>,
    private val onGameChange: (String) -> Unit,
    private val onStatisticsChange: (String) -> Unit,
    private val nowLabel: () -> String,
    private val onStart: () -> Unit = {},
) {
    val game get() = gameState.value
    val canContinue get() = gameState.value?.status == MinesweeperStatus.Playing
    val statistics get() = statisticsState.value

    fun startGame(difficulty: MinesweeperDifficulty) {
        saveGame(MinesweeperGame.start(difficulty))
        onStart()
        saveStatistics(statistics.started(difficulty))
    }

    fun changeGame(next: MinesweeperGame) {
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

    private fun saveGame(game: MinesweeperGame) {
        gameState.value = game
        onGameChange(game.serialize())
    }

    private fun saveStatistics(next: MinesweeperStatistics) {
        if (next == statisticsState.value) return
        statisticsState.value = next
        onStatisticsChange(next.serialize())
    }
}

@Composable
internal fun rememberMinesweeperSession(
    initialGame: String, onGameChange: (String) -> Unit,
    initialStatistics: String, onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String,
    onStart: () -> Unit = {},
): MinesweeperSession {
    val game = rememberSaveable(saver = gameSaver) {
        mutableStateOf(restoreMinesweeperGame(initialGame))
    }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(MinesweeperStatistics.restore(initialStatistics))
    }
    return remember(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart) {
        MinesweeperSession(game, statistics, onGameChange, onStatisticsChange, nowLabel, onStart)
    }
}
