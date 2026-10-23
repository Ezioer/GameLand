package com.zxq.sudoku.games.gomoku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.time.Clock

private val gomokuStatisticsSaver = Saver<MutableState<GomokuStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(GomokuStatistics.restore(it)) },
)

internal class GomokuSession(
    private val gameState: MutableState<GomokuGame?>,
    private val activeStart: MutableState<Pair<Long, String>?>,
    private val statisticsState: MutableState<GomokuStatistics>,
    private val onStatisticsChange: (String) -> Unit,
    private val nowMillis: () -> Long,
    private val nowLabel: () -> String,
) {
    val game get() = gameState.value
    val statistics get() = statisticsState.value

    fun startGame(mode: GomokuMode, level: GomokuLevel? = null) {
        activeStart.value = nowMillis() to nowLabel()
        gameState.value = GomokuGame(mode, level)
    }

    fun changeGame(next: GomokuGame) {
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
internal fun rememberGomokuSession(
    initialStatistics: String,
    onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String,
): GomokuSession {
    val game = remember { mutableStateOf<GomokuGame?>(null) }
    val activeStart = remember { mutableStateOf<Pair<Long, String>?>(null) }
    val statistics = rememberSaveable(saver = gomokuStatisticsSaver) {
        mutableStateOf(GomokuStatistics.restore(initialStatistics))
    }
    return remember(game, activeStart, statistics, onStatisticsChange, nowLabel) {
        GomokuSession(game, activeStart, statistics, onStatisticsChange,
            nowMillis = { Clock.System.now().toEpochMilliseconds() }, nowLabel = nowLabel)
    }
}
