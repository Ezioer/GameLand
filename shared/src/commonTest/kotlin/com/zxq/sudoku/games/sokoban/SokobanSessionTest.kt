package com.zxq.sudoku.games.sokoban

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SokobanSessionTest {
    @Test
    fun progressCanResumeAndRestartCountsNewRound() {
        var savedGame = ""
        var savedStatistics = ""
        val session = SokobanSession(
            gameState = mutableStateOf<SokobanGame?>(null),
            statisticsState = mutableStateOf(SokobanStatistics()),
            onGameChange = { savedGame = it },
            onStatisticsChange = { savedStatistics = it },
            nowLabel = { "2026-10-26" },
        )

        session.startLevel(0)
        val first = requireNotNull(session.game)
        session.changeGame(first.move(SokobanDirection.Right))
        session.tick()
        assertTrue(session.canContinue)
        assertEquals(session.game, restoreSokobanGame(savedGame))
        assertEquals(1, session.statistics.forLevel(0).played)

        session.startLevel(0)
        assertEquals(2, session.statistics.forLevel(0).played)
        assertEquals(0, session.game?.moves)
        assertEquals(0, session.game?.elapsedSeconds)
        assertEquals(session.statistics, SokobanStatistics.restore(savedStatistics))
    }
}
