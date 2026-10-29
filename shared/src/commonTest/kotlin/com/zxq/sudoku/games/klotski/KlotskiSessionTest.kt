package com.zxq.sudoku.games.klotski

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KlotskiSessionTest {
    @Test
    fun continuingKeepsBoardAndStartingAgainCountsNewRound() {
        var savedGame = ""
        var savedStatistics = ""
        val session = KlotskiSession(
            gameState = mutableStateOf<KlotskiGame?>(null),
            statisticsState = mutableStateOf(KlotskiStatistics()),
            onGameChange = { savedGame = it },
            onStatisticsChange = { savedStatistics = it },
            nowLabel = { "2026-10-26" },
        )

        session.startLevel(0)
        session.tick()
        assertTrue(session.canContinue)
        assertEquals(session.game, restoreKlotskiGame(savedGame))
        assertEquals(1, session.statistics.forLevel(0).played)

        session.startLevel(0)
        assertEquals(2, session.statistics.forLevel(0).played)
        assertEquals(0, session.game?.elapsedSeconds)
        assertEquals(session.statistics, KlotskiStatistics.restore(savedStatistics))
    }
}
