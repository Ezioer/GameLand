package com.zxq.sudoku.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

internal data class GamePlayTotals(val rounds: Long = 0, val seconds: Long = 0)

internal data class CollectionPlayStatistics(
    private val games: Map<CollectionGame, GamePlayTotals> = emptyMap(),
) {
    fun forGame(game: CollectionGame): GamePlayTotals = games[game] ?: GamePlayTotals()

    val totalRounds: Long get() = games.values.sumOf { it.rounds }
    val totalSeconds: Long get() = games.values.sumOf { it.seconds }

    fun started(game: CollectionGame): CollectionPlayStatistics = update(game) {
        it.copy(rounds = it.rounds + 1)
    }

    fun tick(game: CollectionGame): CollectionPlayStatistics = update(game) {
        it.copy(seconds = it.seconds + 1)
    }

    private fun update(game: CollectionGame, transform: (GamePlayTotals) -> GamePlayTotals) =
        copy(games = games + (game to transform(forGame(game))))

    fun serialize(): String = "v1|" + CollectionGame.entries.mapNotNull { game ->
        games[game]?.let { "${game.name},${it.rounds},${it.seconds}" }
    }.joinToString(";")

    companion object {
        fun restore(value: String): CollectionPlayStatistics = runCatching {
            if (value.isEmpty()) return CollectionPlayStatistics()
            require(value.startsWith("v1|"))
            val body = value.substringAfter('|')
            if (body.isEmpty()) return CollectionPlayStatistics()
            val games = body.split(';').associate { row ->
                val fields = row.split(',')
                require(fields.size == 3)
                val game = CollectionGame.valueOf(fields[0])
                val rounds = fields[1].toLong().also { require(it >= 0) }
                val seconds = fields[2].toLong().also { require(it >= 0) }
                game to GamePlayTotals(rounds, seconds)
            }
            require(games.size == body.split(';').size)
            CollectionPlayStatistics(games)
        }.getOrDefault(CollectionPlayStatistics())
    }
}

private val playStatisticsSaver = Saver<MutableState<CollectionPlayStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(CollectionPlayStatistics.restore(it)) },
)

internal class CollectionPlaySession(
    private val state: MutableState<CollectionPlayStatistics>,
    private val onChange: (String) -> Unit,
) {
    val statistics get() = state.value

    fun started(game: CollectionGame) = save(statistics.started(game))
    fun tick(game: CollectionGame) = save(statistics.tick(game))

    private fun save(next: CollectionPlayStatistics) {
        state.value = next
        onChange(next.serialize())
    }
}

@Composable
internal fun rememberCollectionPlaySession(
    initialStatistics: String,
    onChange: (String) -> Unit,
): CollectionPlaySession {
    val state = rememberSaveable(saver = playStatisticsSaver) {
        mutableStateOf(CollectionPlayStatistics.restore(initialStatistics))
    }
    return remember(state, onChange) { CollectionPlaySession(state, onChange) }
}

internal fun formatPlayDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = seconds % 3600 / 60
    val remaining = seconds % 60
    return if (hours > 0) "${hours}小时${minutes}分${remaining}秒" else if (minutes > 0) "${minutes}分${remaining}秒"
        else "${remaining}秒"
}
