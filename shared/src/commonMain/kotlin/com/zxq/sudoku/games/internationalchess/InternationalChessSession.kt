package com.zxq.sudoku.games.internationalchess

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.time.Clock

private val statisticsSaver = Saver<MutableState<InternationalChessStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(InternationalChessStatistics.restore(it)) },
)

internal class InternationalChessSession(
    private val gameState: MutableState<ChessGame?>,
    private val activeStart: MutableState<Pair<Long, String>?>,
    private val statisticsState: MutableState<InternationalChessStatistics>,
    private val onStatisticsChange: (String) -> Unit,
    private val nowMillis: () -> Long,
    private val nowLabel: () -> String,
    private val onStart: () -> Unit = {},
) {
    val game get() = gameState.value
    val statistics get() = statisticsState.value

    fun startGame(mode: ChessMode, level: ChessLevel? = null) {
        activeStart.value = nowMillis() to nowLabel()
        gameState.value = ChessGame(mode, level)
        onStart()
    }

    fun changeGame(next: ChessGame) {
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
internal fun rememberInternationalChessSession(
    initialStatistics: String,
    onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String,
    onStart: () -> Unit = {},
): InternationalChessSession {
    val game = remember { mutableStateOf<ChessGame?>(null) }
    val activeStart = remember { mutableStateOf<Pair<Long, String>?>(null) }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(InternationalChessStatistics.restore(initialStatistics))
    }
    return remember(game, activeStart, statistics, onStatisticsChange, nowLabel, onStart) {
        InternationalChessSession(game, activeStart, statistics, onStatisticsChange,
            nowMillis = { Clock.System.now().toEpochMilliseconds() }, nowLabel = nowLabel,
            onStart = onStart)
    }
}
