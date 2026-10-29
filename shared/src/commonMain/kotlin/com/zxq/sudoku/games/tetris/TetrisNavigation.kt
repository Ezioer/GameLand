package com.zxq.sudoku.games.tetris

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
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.tetrisDestinations(
    navController: NavHostController,
    session: TetrisSession,
    match: TetrisMatch,
    returnToMain: () -> Unit,
) {
    composable(AppRoute.TetrisIsland) {
        var showMatchSheet by remember { mutableStateOf(false) }
        val dismissMatchSheet = {
            match.leave()
            showMatchSheet = false
        }
        BackHandler(enabled = showMatchSheet, onBack = dismissMatchSheet)
        BackHandler(enabled = !showMatchSheet, onBack = returnToMain)
        LaunchedEffect(showMatchSheet) {
            if (showMatchSheet) match.pair()
        }
        LaunchedEffect(match.phase, showMatchSheet) {
            if (showMatchSheet && match.phase == MatchPhase.Playing) {
                showMatchSheet = false
                session.startGame()
                session.game?.let(match::publish)
                navController.navigate(AppRoute.TetrisMatchPlay)
            }
        }
        TetrisIslandRoute(session.statistics, session.canContinue, returnToMain,
            onStart = {
                session.startGame()
                navController.navigate(AppRoute.TetrisPlay)
            }, onContinue = {
                if (session.canContinue) {
                    session.resumeGame()
                    navController.navigate(AppRoute.TetrisPlay)
                }
            }, onFriend = {
                showMatchSheet = true
            },
            friendAvailable = match.available)
        if (showMatchSheet) TetrisMatchSheet(match, dismissMatchSheet)
    }
    composable(AppRoute.TetrisPlay) { TetrisDestination(navController, session) }
    composable(AppRoute.TetrisMatchPlay) { TetrisMatchDestination(navController, session, match) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TetrisMatchDestination(navController: NavHostController, session: TetrisSession,
    match: TetrisMatch) {
    val game = session.game
    if (game == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }
    var exiting by remember { mutableStateOf(false) }
    val exit = {
        if (!exiting) {
            exiting = true
            session.changeGame(game.pause())
            match.leave()
            navController.popBackStack()
        }
        Unit
    }
    BackHandler(enabled = !exiting, onBack = exit)
    LaunchedEffect(match, match.phase) {
        if (match.phase == MatchPhase.Playing || match.phase == MatchPhase.Finished) {
            while (true) {
                delay(300)
                match.maintenanceTick()
            }
        }
    }
    LaunchedEffect(match, match.pendingGarbage, match.phase) {
        if (match.phase == MatchPhase.Playing && match.pendingGarbage > 0) {
            val current = session.game ?: return@LaunchedEffect
            val next = current.addGarbage(match.takeIncomingGarbage())
            session.changeGame(next)
            match.publish(next)
        }
    }
    TetrisGameRoute(game, session.statistics, onGameChange = { next ->
        session.changeGame(next)
        match.publish(next)
    }, onRestart = {}, onBack = exit, match = match)
    TetrisMatchResult(match, game, exit)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TetrisDestination(navController: NavHostController, session: TetrisSession) {
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
            session.changeGame(game.pause())
            navController.popBackStack()
        }
        Unit
    }
    BackHandler(enabled = !exiting, onBack = exit)
    TetrisGameRoute(game, session.statistics, session::changeGame, session::startGame, exit)
}
