package com.zxq.sudoku.games.go

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
internal fun NavGraphBuilder.goDestinations(navController: NavHostController,
    session: GoSession, returnToMain: () -> Unit) {
    composable("${AppRoute.GoStatisticsDetail}/{index}") { entry ->
        val index = entry.arguments?.read { getStringOrNull("index")?.toIntOrNull() }
        GoStatisticsDetailScreen(index?.let { session.statistics.records.getOrNull(it) }, returnToMain)
    }
    composable(AppRoute.GoIsland) {
        BackHandler(onBack = returnToMain)
        GoIslandRoute(onBack = returnToMain,
            onHuman = { level ->
                session.startGame(GoMode.Human, level)
                navController.navigate("${AppRoute.GoHuman}/${level.name}")
            },
            onFriend = {
                session.startGame(GoMode.Friend)
                navController.navigate(AppRoute.GoFriend)
            })
    }
    composable("${AppRoute.GoHuman}/{level}") { entry ->
        val level = GoLevel.entries.firstOrNull {
            it.name == entry.arguments?.read { getStringOrNull("level") }
        } ?: GoLevel.Easy
        GoDestination(navController, session, GoMode.Human, level)
    }
    composable(AppRoute.GoFriend) { GoDestination(navController, session, GoMode.Friend) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GoDestination(navController: NavHostController, session: GoSession,
    mode: GoMode, level: GoLevel? = null) {
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
    GoGameRoute(game, session::changeGame, { session.startGame(mode, level) }, exit)
}
