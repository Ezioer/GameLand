package com.zxq.sudoku.games.internationalchess

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.zxq.sudoku.navigation.AppRoute

@OptIn(ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.internationalChessDestinations(
    navController: NavHostController,
    session: InternationalChessSession,
    returnToMain: () -> Unit,
) {
    composable("${AppRoute.InternationalChessStatisticsDetail}/{index}") { entry ->
        val index = entry.arguments?.read { getStringOrNull("index")?.toIntOrNull() }
        InternationalChessStatisticsDetailScreen(
            record = index?.let { session.statistics.records.getOrNull(it) }, onBack = returnToMain)
    }
    composable(AppRoute.InternationalChessIsland) {
        BackHandler(onBack = returnToMain)
        InternationalChessIslandRoute(onBack = returnToMain,
            onHuman = { level ->
                session.startGame(ChessMode.Human, level)
                navController.navigate("${AppRoute.InternationalChessHuman}/${level.name}")
            },
            onFriend = {
                session.startGame(ChessMode.Friend)
                navController.navigate(AppRoute.InternationalChessFriend)
            })
    }
    composable("${AppRoute.InternationalChessHuman}/{level}") { entry ->
        val level = ChessLevel.entries.firstOrNull {
            it.name == entry.arguments?.read { getStringOrNull("level") }
        } ?: ChessLevel.Easy
        InternationalChessDestination(navController, session, ChessMode.Human, level)
    }
    composable(AppRoute.InternationalChessFriend) {
        InternationalChessDestination(navController, session, ChessMode.Friend)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InternationalChessDestination(
    navController: NavHostController,
    session: InternationalChessSession,
    mode: ChessMode,
    level: ChessLevel? = null,
) {
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
            session.leaveGame()
        }
        Unit
    }
    BackHandler(enabled = !exiting, onBack = exit)
    InternationalChessGameRoute(game, session::changeGame,
        onRestart = { session.startGame(mode, level) }, onBack = exit)
}
