package com.zxq.sudoku.games.maze

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
internal fun NavGraphBuilder.mazeDestinations(
    navController: NavHostController,
    session: MazeSession,
    returnToMain: () -> Unit,
) {
    composable(AppRoute.MazeIsland) {
        BackHandler(onBack = returnToMain)
        MazeIslandRoute(session.statistics, session.game, returnToMain,
            onContinue = { if (session.canContinue) navController.navigate(AppRoute.MazePlay) },
            onStart = { difficulty ->
                session.startGame(difficulty)
                navController.navigate(AppRoute.MazePlay)
            })
    }
    composable(AppRoute.MazePlay) { MazeDestination(navController, session) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MazeDestination(navController: NavHostController, session: MazeSession) {
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
    MazeGameRoute(game,
        onMove = { direction -> session.changeGame(game.move(direction)) },
        onUndo = { session.changeGame(game.undo()) },
        onHint = { session.changeGame(game.hint()) },
        onRestart = session::restartGame,
        onNext = {
            val next = MazeDifficulty.entries.getOrElse(game.difficulty.ordinal + 1) { MazeDifficulty.Hard }
            session.startGame(next)
        },
        onTick = session::tick,
        onBack = exit,
    )
}
