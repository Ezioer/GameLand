package com.zxq.sudoku.games.game2048

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Game2048SessionTest {
    @Test
    fun returningKeepsProgressAndStartingAgainCreatesNewRound() {
        var savedGame = ""
        val session = Game2048Session(
            gameState = mutableStateOf<Game2048Game?>(null),
            statisticsState = mutableStateOf(Game2048Statistics()),
            onStatisticsChange = {},
            onGameChange = { savedGame = it },
            nowLabel = { "2026-10-23" },
        )

        session.startGame()
        val played = session.statistics.played
        val current = requireNotNull(session.game)
        val next = SlideDirection.entries.map { current.slide(it) { 0 } }
            .first { it != current }
        session.changeGame(next)
        assertTrue(session.canContinue)
        assertEquals(next, restoreGame2048(savedGame))
        assertEquals(played, session.statistics.played)

        session.startGame()
        assertEquals(played + 1, session.statistics.played)
        assertEquals(0, session.game?.score)
        assertEquals(0, session.game?.moves)
        assertEquals(session.game, restoreGame2048(savedGame))
    }
}
