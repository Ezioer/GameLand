package com.zxq.sudoku.games.gomoku

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
import androidx.savedstate.read
import com.zxq.sudoku.navigation.AppRoute

@OptIn(ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.gomokuDestinations(
    navController: NavHostController,
    session: GomokuSession,
    returnToMain: () -> Unit,
) {
    composable("${AppRoute.GomokuStatisticsDetail}/{index}") { entry ->
        val index = entry.arguments?.read { getStringOrNull("index")?.toIntOrNull() }
        GomokuStatisticsDetailScreen(
            record = index?.let { session.statistics.records.getOrNull(it) },
            onBack = returnToMain,
        )
    }
    composable(AppRoute.GomokuIsland) {
        BackHandler(onBack = returnToMain)
        GomokuIslandRoute(
            onBack = returnToMain,
            onHuman = { level ->
                session.startGame(GomokuMode.Human, level)
                navController.navigate("${AppRoute.GomokuHuman}/${level.name}")
            },
            onFriend = {
                session.startGame(GomokuMode.Friend)
                navController.navigate(AppRoute.GomokuFriend)
            },
        )
    }
    composable("${AppRoute.GomokuHuman}/{level}") { entry ->
        val level = GomokuLevel.entries.firstOrNull {
            it.name == entry.arguments?.read { getStringOrNull("level") }
        } ?: GomokuLevel.Easy
        GomokuDestination(navController, session, GomokuMode.Human, level)
    }
    composable(AppRoute.GomokuFriend) {
        GomokuDestination(navController, session, GomokuMode.Friend)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GomokuDestination(
    navController: NavHostController,
    session: GomokuSession,
    mode: GomokuMode,
    level: GomokuLevel? = null,
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
    GomokuGameRoute(
        game = game,
        onGameChange = session::changeGame,
        onRestart = { session.startGame(mode, level) },
        onBack = exit,
    )
}
