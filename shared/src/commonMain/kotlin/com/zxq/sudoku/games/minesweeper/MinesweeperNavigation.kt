package com.zxq.sudoku.games.minesweeper

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
internal fun NavGraphBuilder.minesweeperDestinations(
    navController: NavHostController,
    session: MinesweeperSession,
    returnToMain: () -> Unit,
) {
    composable(AppRoute.MinesweeperIsland) {
        BackHandler(onBack = returnToMain)
        MinesweeperIslandRoute(session.statistics, session.game, returnToMain,
            onContinue = {
                if (session.canContinue) navController.navigate(AppRoute.MinesweeperPlay)
            },
            onStart = { difficulty ->
                session.startGame(difficulty)
                navController.navigate(AppRoute.MinesweeperPlay)
            })
    }
    composable(AppRoute.MinesweeperPlay) { MinesweeperDestination(navController, session) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MinesweeperDestination(navController: NavHostController, session: MinesweeperSession) {
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
    MinesweeperGameRoute(game,
        onReveal = { index -> session.changeGame(game.reveal(index)) },
        onFlag = { index -> session.changeGame(game.toggleFlag(index)) },
        onChord = { index -> session.changeGame(game.chord(index)) },
        onRestart = { session.startGame(game.difficulty) },
        onTick = session::tick,
        onBack = exit,
    )
}
