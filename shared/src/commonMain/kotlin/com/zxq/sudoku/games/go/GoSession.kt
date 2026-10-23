package com.zxq.sudoku.games.go

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.time.Clock

private val goStatisticsSaver = Saver<MutableState<GoStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(GoStatistics.restore(it)) },
)

internal class GoSession(
    private val gameState: MutableState<GoGame?>,
    private val activeStart: MutableState<Pair<Long, String>?>,
    private val statisticsState: MutableState<GoStatistics>,
    private val onStatisticsChange: (String) -> Unit,
    private val nowMillis: () -> Long,
    private val nowLabel: () -> String,
) {
    val game get() = gameState.value
    val statistics get() = statisticsState.value

    fun startGame(mode: GoMode, level: GoLevel? = null) {
        activeStart.value = nowMillis() to nowLabel()
        gameState.value = GoGame(mode, level)
    }

    fun changeGame(next: GoGame) {
        val previous = gameState.value ?: return
        if (previous.finished) return
        gameState.value = next
        if (next.finished) {
            activeStart.value?.let { (startedMillis, startedAt) ->
                val updated = statistics.completed(next, startedMillis, startedAt, nowMillis(), nowLabel())
                statisticsState.value = updated
                onStatisticsChange(updated.serialize())
            }
            activeStart.value = null
        }
    }

    fun leaveGame() {
        activeStart.value = null
        gameState.value = null
    }
}

@Composable
internal fun rememberGoSession(
    initialStatistics: String,
    onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String,
): GoSession {
    val game = remember { mutableStateOf<GoGame?>(null) }
    val activeStart = remember { mutableStateOf<Pair<Long, String>?>(null) }
    val statistics = rememberSaveable(saver = goStatisticsSaver) {
        mutableStateOf(GoStatistics.restore(initialStatistics))
    }
    return remember(game, activeStart, statistics, onStatisticsChange, nowLabel) {
        GoSession(game, activeStart, statistics, onStatisticsChange,
            nowMillis = { Clock.System.now().toEpochMilliseconds() }, nowLabel = nowLabel)
    }
}
