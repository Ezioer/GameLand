package com.zxq.sudoku.games.klotski

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
internal fun NavGraphBuilder.klotskiDestinations(navController: NavHostController,
    session: KlotskiSession, returnToMain: () -> Unit) {
    composable(AppRoute.KlotskiIsland) {
        BackHandler(onBack = returnToMain)
        KlotskiIslandRoute(session.statistics, session.game, onBack = returnToMain,
            onContinue = {
                if (session.canContinue) navController.navigate(AppRoute.KlotskiPlay)
            }, onStartLevel = { index ->
                session.startLevel(index)
                navController.navigate(AppRoute.KlotskiPlay)
            })
    }
    composable(AppRoute.KlotskiPlay) { KlotskiDestination(navController, session) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun KlotskiDestination(navController: NavHostController, session: KlotskiSession) {
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
    KlotskiGameRoute(game,
        onMove = { piece, direction, steps -> session.changeGame(game.move(piece, direction, steps)) },
        onUndo = { session.changeGame(game.undo()) },
        onRestart = { session.startLevel(game.levelIndex) },
        onNext = { if (game.levelIndex < KlotskiLevels.lastIndex) session.startLevel(game.levelIndex + 1) },
        onTick = session::tick,
        onBack = exit,
    )
}
