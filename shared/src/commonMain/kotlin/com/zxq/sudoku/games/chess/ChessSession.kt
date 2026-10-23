package com.zxq.sudoku.games.chess

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.time.Clock

private val chessStatisticsSaver = Saver<MutableState<ChessStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(ChessStatistics.restore(it)) },
)

internal class ChessSession(
    private val gameState: MutableState<ChessGame?>,
    private val activeStart: MutableState<Pair<Long, String>?>,
    private val statisticsState: MutableState<ChessStatistics>,
    private val onStatisticsChange: (String) -> Unit,
    private val nowMillis: () -> Long,
    private val nowLabel: () -> String,
) {
    val game get() = gameState.value
    val statistics get() = statisticsState.value

    private fun save(next: ChessStatistics) {
        if (next == statisticsState.value) return
        statisticsState.value = next
        onStatisticsChange(next.serialize())
    }

    fun startGame(mode: ChessMode, level: ChessLevel? = null, puzzleIndex: Int? = null) {
        activeStart.value = nowMillis() to nowLabel()
        gameState.value = newChessGame(mode, level, puzzleIndex)
    }

    fun changeGame(next: ChessGame) {
        if (gameState.value == null) return
        gameState.value = next
        if (next.finished) {
            activeStart.value?.let { (startedMillis, startedAt) ->
                save(statistics.completed(next, startedMillis, startedAt, nowMillis(), nowLabel()))
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
internal fun rememberChessSession(
    initialStatistics: String,
    onStatisticsChange: (String) -> Unit,
    nowLabel: () -> String,
): ChessSession {
    val game = remember { mutableStateOf<ChessGame?>(null) }
    val activeStart = remember { mutableStateOf<Pair<Long, String>?>(null) }
    val statistics = rememberSaveable(saver = chessStatisticsSaver) {
        mutableStateOf(ChessStatistics.restore(initialStatistics))
    }
    return remember(game, activeStart, statistics, onStatisticsChange, nowLabel) {
        ChessSession(game, activeStart, statistics, onStatisticsChange,
            nowMillis = { Clock.System.now().toEpochMilliseconds() }, nowLabel = nowLabel)
    }
}
