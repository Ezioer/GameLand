package com.zxq.sudoku.games.chess

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.zxq.sudoku.navigation.AppRoute

@OptIn(ExperimentalComposeUiApi::class)
internal fun NavGraphBuilder.chessDestinations(
    navController: NavHostController,
    session: ChessSession,
    returnToMain: () -> Unit,
) {
    composable("${AppRoute.ChessStatisticsDetail}/{index}") { entry ->
//        val index = entry.arguments?.getString("index")?.toIntOrNull()
        val index = entry.arguments?.read {
            getStringOrNull("index")?.toIntOrNull()
        }
        ChessStatisticsDetailScreen(
            record = index?.let { session.statistics.completedRecords.getOrNull(it) },
            onBack = returnToMain,
        )
    }
    composable(AppRoute.ChessIsland) {
        BackHandler(onBack = returnToMain)
        ChessIslandRoute(
            onBack = returnToMain,
            onHuman = { level ->
                session.startGame(ChessMode.Human, level)
                navController.navigate("${AppRoute.ChessHuman}/${level.name}")
            },
            onFriend = {
                session.startGame(ChessMode.Friend)
                navController.navigate(AppRoute.ChessFriend)
            },
            onPuzzle = { index ->
                session.startGame(ChessMode.Puzzle, puzzleIndex = index)
                navController.navigate("${AppRoute.ChessPuzzle}/$index")
            },
        )
    }
    composable("${AppRoute.ChessHuman}/{level}") { entry ->
        val level = ChessLevel.entries.firstOrNull { it.name == entry.arguments?.read { getStringOrNull("level") } } ?: ChessLevel.Easy
        ChessDestination(navController, session, ChessMode.Human, level)
    }
    composable(AppRoute.ChessFriend) {
        ChessDestination(navController, session, ChessMode.Friend)
    }
    composable("${AppRoute.ChessPuzzle}/{index}") { entry ->
//        val index = entry.arguments?.getString("index")?.toIntOrNull()?.coerceIn(0, ChessPuzzles.entries.lastIndex) ?: 0
        val index = entry.arguments?.read { getStringOrNull("index")?.toIntOrNull() }?.coerceIn(0, ChessPuzzles.entries.lastIndex) ?: 0
        ChessDestination(navController, session, ChessMode.Puzzle, puzzleIndex = index)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChessDestination(
    navController: NavHostController,
    session: ChessSession,
    mode: ChessMode,
    level: ChessLevel? = null,
    puzzleIndex: Int? = null,
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
    ChessGameRoute(
        game = game,
        onGameChange = session::changeGame,
        onRestart = { session.startGame(mode, level, puzzleIndex) },
        onBack = exit,
    )
}
