package com.zxq.sudoku.games.sokoban

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.zxq.sudoku.navigation.AppRoute

@OptIn(ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.sokobanDestinations(navController: NavHostController,
    session: SokobanSession, returnToMain: () -> Unit) {
    composable(AppRoute.SokobanIsland) {
        BackHandler(onBack = returnToMain)
        SokobanIslandRoute(session.statistics, session.game, onBack = returnToMain,
            onContinue = {
                if (session.canContinue) navController.navigate(AppRoute.SokobanPlay)
            }, onStartLevel = { index ->
                session.startLevel(index)
                navController.navigate(AppRoute.SokobanPlay)
            })
    }
    composable(AppRoute.SokobanPlay) { SokobanDestination(navController, session) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SokobanDestination(navController: NavHostController, session: SokobanSession) {
    val game = session.game
    if (game == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }
    var exiting by remember { mutableStateOf(false) }
    val exit = {
        if (!exiting) {
            exiting = true
            navController.popBackStack()
        }
        Unit
    }
    BackHandler(enabled = !exiting, onBack = exit)
    SokobanGameRoute(game,
        onMove = { direction -> session.changeGame(game.move(direction)) },
        onUndo = { session.changeGame(game.undo()) },
        onRestart = { session.startLevel(game.levelIndex) },
        onNext = { if (game.levelIndex < SokobanLevels.lastIndex) session.startLevel(game.levelIndex + 1) },
        onTick = session::tick,
        onBack = exit,
    )
}
