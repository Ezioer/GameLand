package com.zxq.sudoku.games.sudoku

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.zxq.sudoku.navigation.AppRoute

@OptIn(ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.sudokuDestinations(
    navController: NavHostController,
    session: SudokuSession,
    returnToMain: () -> Unit,
) {
    composable("${AppRoute.SudokuStatisticsDetail}/{difficulty}") { entry ->
        val difficulty = SudokuDifficulty.entries.firstOrNull {
            it.name == entry.arguments?.read { getString("difficulty") }
        }
        SudokuStatisticsDetailScreen(session.statistics, difficulty, onBack = returnToMain)
    }
    composable(AppRoute.SudokuIsland) {
        BackHandler(onBack = returnToMain)
        SudokuIslandRoute(
            onBack = returnToMain,
            onStartClassic = { difficulty ->
                session.startGame(difficulty)
                navController.navigate(AppRoute.SudokuSingle)
            },
            onStartBattle = { difficulty ->
                session.startBattle(difficulty)
                navController.navigate(AppRoute.SudokuBattle)
            },
        )
    }
    composable(AppRoute.SudokuSingle) {
        val lastGame = remember { mutableStateOf(session.game) }
        SideEffect { session.game?.let { lastGame.value = it } }
        val game = session.game ?: lastGame.value
        if (game == null) {
            LaunchedEffect(Unit) { navController.popBackStack() }
        } else {
            SudokuGameTimer(session)
            var exiting by remember { mutableStateOf(false) }
            val exitGame = {
                if (!exiting) {
                    exiting = true
                    navController.popBackStack()
                    session.leaveGame()
                }
                Unit
            }
            BackHandler(enabled = !exiting, onBack = exitGame)
            SudokuScreen(
                game = game,
                difficultyStatistics = session.statistics.forDifficulty(game.puzzle.difficulty),
                palette = session.palette,
                fontSize = session.fontSize,
                onPaletteChange = { session.palette = it },
                onFontSizeChange = { session.fontSize = it },
                onGameChange = session::changeGame,
                onNoteToggle = { session.requestNoteToggle(NoteTutorialTarget.Classic) },
                onRestart = { session.startGame(game.puzzle.difficulty) },
                onExit = exitGame,
            )
            if (session.showBeginnerTutorial) {
                BeginnerTutorialDialog(onFinished = session::finishBeginnerTutorial)
            }
            if (session.noteTutorialTarget != null) {
                NoteTutorialDialog(
                    onDismiss = session::dismissNoteTutorial,
                    onConfirm = session::confirmNoteTutorial,
                )
            }
        }
    }
    composable(AppRoute.SudokuBattle) {
        val lastBattle = remember { mutableStateOf(session.battle) }
        SideEffect { session.battle?.let { lastBattle.value = it } }
        val battle = session.battle ?: lastBattle.value
        if (battle == null) {
            LaunchedEffect(Unit) { navController.popBackStack() }
        } else {
            SudokuBattleTimer(session)
            var exiting by remember { mutableStateOf(false) }
            val exitBattle = {
                if (!exiting) {
                    exiting = true
                    navController.popBackStack()
                    session.leaveBattle()
                }
                Unit
            }
            BackHandler(enabled = !exiting, onBack = exitBattle)
            BattleScreen(
                battle = battle,
                palette = session.palette,
                fontSize = session.fontSize,
                onPaletteChange = { session.palette = it },
                onFontSizeChange = { session.fontSize = it },
                onBattleChange = session::changeBattle,
                onNoteToggle = { player ->
                    session.requestNoteToggle(
                        if (player == 1) NoteTutorialTarget.PlayerOne else NoteTutorialTarget.PlayerTwo,
                    )
                },
                onRestart = { session.startBattle(battle.first.puzzle.difficulty) },
                onExit = exitBattle,
            )
            if (session.showBeginnerTutorial) {
                BeginnerTutorialDialog(onFinished = session::finishBeginnerTutorial)
            }
            if (session.noteTutorialTarget != null) {
                NoteTutorialDialog(
                    onDismiss = session::dismissNoteTutorial,
                    onConfirm = session::confirmNoteTutorial,
                )
            }
        }
    }
}
