package com.zxq.sudoku.games.sudoku

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SudokuSessionTest {
    @Test
    fun startingAndLeavingSingleGameKeepsStatisticsAndTutorial() {
        var tutorialSeenCalls = 0
        var serializedStatistics = ""
        val session = SudokuSession(
            gameState = mutableStateOf<SudokuGame?>(null),
            battleState = mutableStateOf<SudokuBattle?>(null),
            statisticsState = mutableStateOf(GameStatistics()),
            paletteState = mutableStateOf(GamePalette.Island),
            fontSizeState = mutableStateOf(GameFontSize.Medium),
            beginnerSeenState = mutableStateOf(false),
            beginnerTutorialState = mutableStateOf(false),
            noteSeenState = mutableStateOf(false),
            noteTargetState = mutableStateOf<NoteTutorialTarget?>(null),
            onBeginnerTutorialSeen = { tutorialSeenCalls++ },
            onNoteTutorialSeen = {},
            onStatisticsChange = { serializedStatistics = it },
            currentDate = { "2026-10-22" },
        )

        session.startGame(SudokuDifficulty.Beginner)
        assertEquals(1, session.statistics.forDifficulty(SudokuDifficulty.Beginner).played)
        assertEquals("2026-10-22", session.statistics.gamesFor(SudokuDifficulty.Beginner).single().date)
        assertTrue(session.showBeginnerTutorial)
        session.finishBeginnerTutorial()
        assertEquals(1, tutorialSeenCalls)
        assertFalse(session.showBeginnerTutorial)

        session.leaveGame()
        assertNull(session.game)
        assertEquals(GameResult.Left, session.statistics.gamesFor(SudokuDifficulty.Beginner).single().result)
        assertEquals(session.statistics.serialize(), serializedStatistics)
    }
}
