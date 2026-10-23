package com.zxq.sudoku.games.sudoku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay

private val gameSaver = Saver<MutableState<SudokuGame?>, String>(
    save = { it.value?.serialize().orEmpty() },
    restore = { mutableStateOf(SudokuGame.restore(it)) },
)
private val battleSaver = Saver<MutableState<SudokuBattle?>, String>(
    save = { it.value?.serialize().orEmpty() },
    restore = { mutableStateOf(SudokuBattle.restore(it)) },
)
private val statisticsSaver = Saver<MutableState<GameStatistics>, String>(
    save = { it.value.serialize() },
    restore = { mutableStateOf(GameStatistics.restore(it)) },
)

internal enum class NoteTutorialTarget { Classic, PlayerOne, PlayerTwo }

internal class SudokuSession(
    private val gameState: MutableState<SudokuGame?>,
    private val battleState: MutableState<SudokuBattle?>,
    private val statisticsState: MutableState<GameStatistics>,
    private val paletteState: MutableState<GamePalette>,
    private val fontSizeState: MutableState<GameFontSize>,
    private val beginnerSeenState: MutableState<Boolean>,
    private val beginnerTutorialState: MutableState<Boolean>,
    private val noteSeenState: MutableState<Boolean>,
    private val noteTargetState: MutableState<NoteTutorialTarget?>,
    private val onBeginnerTutorialSeen: () -> Unit,
    private val onNoteTutorialSeen: () -> Unit,
    private val onStatisticsChange: (String) -> Unit,
    private val currentDate: () -> String,
) {
    val game: SudokuGame? get() = gameState.value
    val battle: SudokuBattle? get() = battleState.value
    val statistics: GameStatistics get() = statisticsState.value
    var palette: GamePalette
        get() = paletteState.value
        set(value) { paletteState.value = value }
    var fontSize: GameFontSize
        get() = fontSizeState.value
        set(value) { fontSizeState.value = value }
    val showBeginnerTutorial: Boolean get() = beginnerTutorialState.value
    val noteTutorialTarget: NoteTutorialTarget? get() = noteTargetState.value

    private fun saveStatistics(updated: GameStatistics) {
        statisticsState.value = updated
        onStatisticsChange(updated.serialize())
    }

    fun startGame(difficulty: SudokuDifficulty) {
        game?.let { saveStatistics(statistics.left(it)) }
        gameState.value = SudokuGame.start(SudokuPuzzle.create(difficulty))
        saveStatistics(statistics.started(difficulty, currentDate()))
        beginnerTutorialState.value = difficulty == SudokuDifficulty.Beginner && !beginnerSeenState.value
    }

    fun startBattle(difficulty: SudokuDifficulty) {
        battleState.value = SudokuBattle.start(SudokuPuzzle.create(difficulty))
        beginnerTutorialState.value = difficulty == SudokuDifficulty.Beginner && !beginnerSeenState.value
    }

    fun leaveGame() {
        game?.let { saveStatistics(statistics.left(it)) }
        gameState.value = null
        beginnerTutorialState.value = false
        noteTargetState.value = null
    }

    fun leaveBattle() {
        battleState.value = null
        beginnerTutorialState.value = false
        noteTargetState.value = null
    }

    fun changeGame(next: SudokuGame) {
        game?.let { previous ->
            val updated = statistics.afterGameChange(previous, next)
            if (updated != statistics) saveStatistics(updated)
        }
        gameState.value = next
    }

    fun changeBattle(next: SudokuBattle) { battleState.value = next }

    fun tickGame() {
        game?.let { previous ->
            val next = previous.tick()
            gameState.value = next
            val updated = statistics.afterGameChange(previous, next)
            if (updated != statistics) saveStatistics(updated)
        }
    }

    fun tickBattle() { battleState.value = battle?.tick() }

    fun finishBeginnerTutorial() {
        beginnerTutorialState.value = false
        beginnerSeenState.value = true
        onBeginnerTutorialSeen()
    }

    fun dismissNoteTutorial() { noteTargetState.value = null }

    private fun toggleNotes(target: NoteTutorialTarget) {
        when (target) {
            NoteTutorialTarget.Classic -> gameState.value = game?.toggleNotes()
            NoteTutorialTarget.PlayerOne -> battleState.value = battle?.updatePlayer(1) { it.toggleNotes() }
            NoteTutorialTarget.PlayerTwo -> battleState.value = battle?.updatePlayer(2) { it.toggleNotes() }
        }
    }

    fun requestNoteToggle(target: NoteTutorialTarget) {
        val notesEnabled = when (target) {
            NoteTutorialTarget.Classic -> game?.noteMode
            NoteTutorialTarget.PlayerOne -> battle?.first?.noteMode
            NoteTutorialTarget.PlayerTwo -> battle?.second?.noteMode
        } == true
        if (noteSeenState.value || notesEnabled) toggleNotes(target)
        else noteTargetState.value = target
    }

    fun confirmNoteTutorial() {
        val target = noteTargetState.value ?: return
        noteTargetState.value = null
        noteSeenState.value = true
        onNoteTutorialSeen()
        toggleNotes(target)
    }
}

@Composable
internal fun rememberSudokuSession(
    initialBeginnerTutorialSeen: Boolean,
    onBeginnerTutorialSeen: () -> Unit,
    initialNoteTutorialSeen: Boolean,
    onNoteTutorialSeen: () -> Unit,
    initialStatistics: String,
    onStatisticsChange: (String) -> Unit,
    currentDate: () -> String,
): SudokuSession {
    val game = rememberSaveable(saver = gameSaver) { mutableStateOf<SudokuGame?>(null) }
    val battle = rememberSaveable(saver = battleSaver) { mutableStateOf<SudokuBattle?>(null) }
    val statistics = rememberSaveable(saver = statisticsSaver) {
        mutableStateOf(GameStatistics.restore(initialStatistics))
    }
    val palette = rememberSaveable { mutableStateOf(GamePalette.Island) }
    val fontSize = rememberSaveable { mutableStateOf(GameFontSize.Medium) }
    val beginnerSeen = rememberSaveable { mutableStateOf(initialBeginnerTutorialSeen) }
    val beginnerTutorial = rememberSaveable { mutableStateOf(false) }
    val noteSeen = rememberSaveable { mutableStateOf(initialNoteTutorialSeen) }
    val noteTarget = rememberSaveable { mutableStateOf<NoteTutorialTarget?>(null) }
    return SudokuSession(
        game, battle, statistics, palette, fontSize, beginnerSeen, beginnerTutorial, noteSeen, noteTarget,
        onBeginnerTutorialSeen, onNoteTutorialSeen, onStatisticsChange, currentDate,
    )
}

@Composable
internal fun SudokuGameTimer(session: SudokuSession) {
    val latestSession by rememberUpdatedState(session)
    LaunchedEffect(session.game != null, session.game?.finished, session.game?.paused,
        session.showBeginnerTutorial, session.noteTutorialTarget) {
        while (latestSession.game != null && latestSession.game?.finished == false &&
            latestSession.game?.paused == false && !latestSession.showBeginnerTutorial &&
            latestSession.noteTutorialTarget == null) {
            delay(1_000)
            latestSession.tickGame()
        }
    }
}

@Composable
internal fun SudokuBattleTimer(session: SudokuSession) {
    val latestSession by rememberUpdatedState(session)
    LaunchedEffect(session.battle != null, session.battle?.finished, session.battle?.paused,
        session.showBeginnerTutorial, session.noteTutorialTarget) {
        while (latestSession.battle != null && latestSession.battle?.finished == false &&
            latestSession.battle?.paused == false && !latestSession.showBeginnerTutorial &&
            latestSession.noteTutorialTarget == null) {
            delay(1_000)
            latestSession.tickBattle()
        }
    }
}
