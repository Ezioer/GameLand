package com.zxq.sudoku.games.game2048

import androidx.compose.runtime.Composable
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
import com.zxq.sudoku.navigation.AppRoute

@OptIn(ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.game2048Destinations(navController: NavHostController,
    session: Game2048Session, returnToMain: () -> Unit) {
    composable(AppRoute.Game2048Island) {
        BackHandler(onBack = returnToMain)
        Game2048IslandRoute(session.statistics, session.canContinue, onBack = returnToMain,
            onStart = {
                session.startGame()
                navController.navigate(AppRoute.Game2048Play)
            }, onContinue = {
                if (session.canContinue) navController.navigate(AppRoute.Game2048Play)
            })
    }
    composable(AppRoute.Game2048Play) { Game2048Destination(navController, session) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Game2048Destination(navController: NavHostController, session: Game2048Session) {
    val lastGame = remember { mutableStateOf(session.game) }
    SideEffect { session.game?.let { lastGame.value = it } }
    val game = session.game ?: lastGame.value
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
    Game2048GameRoute(game, session.statistics, session::changeGame, session::startGame, exit)
}
