package com.zxq.sudoku.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zxq.sudoku.collection.CollectionHomeRoute
import com.zxq.sudoku.collection.CollectionGame
import com.zxq.sudoku.collection.CollectionPlaySession
import com.zxq.sudoku.collection.CollectionStatisticsScreen
import com.zxq.sudoku.projects.ProjectsHomeScreen
import com.zxq.sudoku.games.minesweeper.MinesweeperStatus
import com.zxq.sudoku.games.chess.chessDestinations
import com.zxq.sudoku.games.chess.ChessSession
import com.zxq.sudoku.games.gomoku.GomokuSession
import com.zxq.sudoku.games.gomoku.gomokuDestinations
import com.zxq.sudoku.games.go.GoSession
import com.zxq.sudoku.games.go.goDestinations
import com.zxq.sudoku.games.game2048.Game2048Session
import com.zxq.sudoku.games.game2048.game2048Destinations
import com.zxq.sudoku.games.sokoban.SokobanSession
import com.zxq.sudoku.games.sokoban.sokobanDestinations
import com.zxq.sudoku.games.klotski.KlotskiSession
import com.zxq.sudoku.games.klotski.klotskiDestinations
import com.zxq.sudoku.games.internationalchess.InternationalChessSession
import com.zxq.sudoku.games.internationalchess.internationalChessDestinations
import com.zxq.sudoku.games.tetris.TetrisSession
import com.zxq.sudoku.games.tetris.TetrisMatch
import com.zxq.sudoku.games.tetris.tetrisDestinations
import com.zxq.sudoku.games.maze.MazeSession
import com.zxq.sudoku.games.maze.mazeDestinations
import com.zxq.sudoku.games.minesweeper.MinesweeperSession
import com.zxq.sudoku.games.minesweeper.minesweeperDestinations
import com.zxq.sudoku.games.sudoku.SudokuSession
import com.zxq.sudoku.games.sudoku.sudokuDestinations
import com.zxq.sudoku.games.sudoku.GamePalette
import com.zxq.sudoku.settings.SettingsScreen
import com.zxq.sudoku.theme.AppAppearance
import com.zxq.sudoku.theme.ThemeMode
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

internal object AppRoute {
    const val Projects = "projects"
    const val Home = "home"
    const val Statistics = "statistics"
    const val Settings = "settings"
    const val SudokuStatisticsDetail = "statistics/sudoku"
    const val ChessStatisticsDetail = "statistics/chess"
    const val SudokuIsland = "sudoku/island"
    const val SudokuSingle = "sudoku/single"
    const val SudokuBattle = "sudoku/battle"
    const val ChessIsland = "chess/island"
    const val ChessHuman = "chess/human"
    const val ChessFriend = "chess/friend"
    const val ChessPuzzle = "chess/puzzle"
    const val GomokuIsland = "gomoku/island"
    const val GomokuHuman = "gomoku/human"
    const val GomokuFriend = "gomoku/friend"
    const val GomokuStatisticsDetail = "statistics/gomoku"
    const val GoIsland = "go/island"
    const val GoHuman = "go/human"
    const val GoFriend = "go/friend"
    const val GoStatisticsDetail = "statistics/go"
    const val Game2048Island = "game2048/island"
    const val Game2048Play = "game2048/play"
    const val SokobanIsland = "sokoban/island"
    const val SokobanPlay = "sokoban/play"
    const val KlotskiIsland = "klotski/island"
    const val KlotskiPlay = "klotski/play"
    const val InternationalChessIsland = "internationalchess/island"
    const val InternationalChessHuman = "internationalchess/human"
    const val InternationalChessFriend = "internationalchess/friend"
    const val InternationalChessStatisticsDetail = "statistics/internationalchess"
    const val TetrisIsland = "tetris/island"
    const val TetrisPlay = "tetris/play"
    const val TetrisMatchPlay = "tetris/match/play"
    const val MazeIsland = "maze/island"
    const val MazePlay = "maze/play"
    const val MinesweeperIsland = "minesweeper/island"
    const val MinesweeperPlay = "minesweeper/play"
}

private enum class MainTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(AppRoute.Home, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    Statistics(AppRoute.Statistics, "统计", Icons.Filled.PieChart, Icons.Outlined.PieChart),
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun AppNavigation(
    session: SudokuSession,
    chessSession: ChessSession,
    gomokuSession: GomokuSession,
    goSession: GoSession,
    game2048Session: Game2048Session,
    sokobanSession: SokobanSession,
    klotskiSession: KlotskiSession,
    internationalChessSession: InternationalChessSession,
    tetrisSession: TetrisSession,
    mazeSession: MazeSession,
    minesweeperSession: MinesweeperSession,
    playSession: CollectionPlaySession,
    isAppForeground: Boolean,
    appearance: AppAppearance,
    darkTheme: Boolean,
    tetrisMatch: TetrisMatch,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: AppRoute.Projects
    val activeGame = when {
        !isAppForeground -> null
        currentRoute == AppRoute.SudokuSingle && session.game?.finished == false &&
            session.game?.paused == false && !session.showBeginnerTutorial &&
            session.noteTutorialTarget == null -> CollectionGame.Sudoku
        currentRoute == AppRoute.SudokuBattle && session.battle?.finished == false &&
            session.battle?.paused == false && !session.showBeginnerTutorial &&
            session.noteTutorialTarget == null -> CollectionGame.Sudoku
        (currentRoute.startsWith("${AppRoute.ChessHuman}/") ||
            currentRoute == AppRoute.ChessFriend || currentRoute.startsWith("${AppRoute.ChessPuzzle}/")) &&
            chessSession.game?.finished == false -> CollectionGame.ChineseChess
        (currentRoute.startsWith("${AppRoute.GomokuHuman}/") ||
            currentRoute == AppRoute.GomokuFriend) && gomokuSession.game?.finished == false -> CollectionGame.Gomoku
        (currentRoute.startsWith("${AppRoute.GoHuman}/") ||
            currentRoute == AppRoute.GoFriend) && goSession.game?.finished == false -> CollectionGame.Go
        currentRoute == AppRoute.Game2048Play && game2048Session.game?.finished == false &&
            game2048Session.game?.won == false -> CollectionGame.Game2048
        currentRoute == AppRoute.SokobanPlay && sokobanSession.game?.completed == false -> CollectionGame.Sokoban
        currentRoute == AppRoute.KlotskiPlay && klotskiSession.game?.completed == false -> CollectionGame.Klotski
        (currentRoute.startsWith("${AppRoute.InternationalChessHuman}/") ||
            currentRoute == AppRoute.InternationalChessFriend) &&
            internationalChessSession.game?.finished == false -> CollectionGame.InternationalChess
        currentRoute == AppRoute.TetrisPlay && tetrisSession.game?.playable == true -> CollectionGame.Tetris
        currentRoute == AppRoute.TetrisMatchPlay && tetrisSession.game?.playable == true -> CollectionGame.Tetris
        currentRoute == AppRoute.MazePlay && mazeSession.game?.completed == false -> CollectionGame.Maze
        currentRoute == AppRoute.MinesweeperPlay && minesweeperSession.game?.started == true &&
            minesweeperSession.game?.status == MinesweeperStatus.Playing -> CollectionGame.Minesweeper
        else -> null
    }
    LaunchedEffect(activeGame, playSession) {
        val game = activeGame ?: return@LaunchedEffect
        while (true) {
            delay(1_000)
            playSession.tick(game)
        }
    }
    val showMainNavigation = MainTab.entries.any { it.route == currentRoute }
    val navigationSuiteState = rememberNavigationSuiteScaffoldState(
        initialValue = if (showMainNavigation) NavigationSuiteScaffoldValue.Visible else NavigationSuiteScaffoldValue.Hidden,
    )
    val navigationScope = rememberCoroutineScope()
    val returnToMain: () -> Unit = {
        navigationScope.launch(start = CoroutineStart.UNDISPATCHED) { navigationSuiteState.show() }
        navController.navigateUp()
        Unit
    }
    val returnToProjects: () -> Unit = {
        navigationScope.launch(start = CoroutineStart.UNDISPATCHED) { navigationSuiteState.hide() }
        navController.navigateUp()
        Unit
    }
    LaunchedEffect(navController, navigationSuiteState) {
        navController.currentBackStackEntryFlow.collectLatest { entry ->
            if (MainTab.entries.any { it.route == entry.destination.route }) {
                if (navigationSuiteState.targetValue != NavigationSuiteScaffoldValue.Visible) navigationSuiteState.show()
            } else {
                if (navigationSuiteState.targetValue != NavigationSuiteScaffoldValue.Hidden) navigationSuiteState.hide()
            }
        }
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()),
        state = navigationSuiteState,
        navigationItemVerticalArrangement = Arrangement.Center,
        navigationItems = {
            MainTab.entries.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationSuiteItem(
                    icon = { Icon(if (selected) tab.selectedIcon else tab.unselectedIcon, contentDescription = null) },
                    label = { Text(tab.label) },
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(tab.route) {
                                popUpTo(AppRoute.Home) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Projects,
            enterTransition = {
                fadeIn(tween(280)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { -it / 12 }
            },
            exitTransition = {
                fadeOut(tween(180)) + slideOutVertically(tween(260, easing = FastOutSlowInEasing)) { it / 12 }
            },
            popEnterTransition = {
                fadeIn(tween(280)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 12 }
            },
            popExitTransition = {
                fadeOut(tween(180)) + slideOutVertically(tween(260, easing = FastOutSlowInEasing)) { -it / 12 }
            },
        ) {
            composable(AppRoute.Projects) {
                ProjectsHomeScreen(onGameIslandClick = { navController.navigate(AppRoute.Home) })
            }
            composable(AppRoute.Home) {
                BackHandler(onBack = returnToProjects)
                CollectionHomeRoute(
                    statistics = session.statistics,
                    onBack = returnToProjects,
                    onSudokuClick = { navController.navigate(AppRoute.SudokuIsland) },
                    onChessClick = { navController.navigate(AppRoute.ChessIsland) },
                    onGomokuClick = { navController.navigate(AppRoute.GomokuIsland) },
                    onGoClick = { navController.navigate(AppRoute.GoIsland) },
                    onGame2048Click = { navController.navigate(AppRoute.Game2048Island) },
                    onSokobanClick = { navController.navigate(AppRoute.SokobanIsland) },
                    onKlotskiClick = { navController.navigate(AppRoute.KlotskiIsland) },
                    onInternationalChessClick = { navController.navigate(AppRoute.InternationalChessIsland) },
                    onTetrisClick = { navController.navigate(AppRoute.TetrisIsland) },
                    onMazeClick = { navController.navigate(AppRoute.MazeIsland) },
                    onMinesweeperClick = { navController.navigate(AppRoute.MinesweeperIsland) },
                    onStatisticsClick = { navController.navigate(AppRoute.Statistics) },
                    onSettingsClick = { navController.navigate(AppRoute.Settings) },
                )
            }
            composable(AppRoute.Settings) {
                BackHandler(onBack = returnToMain)
                SettingsScreen(appearance.mode, appearance.palette,
                    onModeChange = { appearance.mode = it },
                    onPaletteChange = { appearance.palette = it },
                    onBack = returnToMain)
            }
            composable(AppRoute.Statistics) {
                CollectionStatisticsScreen(
                    session.statistics,
                    chessSession.statistics,
                    gomokuSession.statistics,
                    goSession.statistics,
                    game2048Session.statistics,
                    sokobanSession.statistics,
                    klotskiSession.statistics,
                    internationalChessSession.statistics,
                    tetrisSession.statistics,
                    mazeSession.statistics,
                    minesweeperSession.statistics,
                    playSession.statistics,
                    onSudokuDifficultyClick = { difficulty ->
                        navController.navigate("${AppRoute.SudokuStatisticsDetail}/${difficulty.name}")
                    },
                    onChessRecordClick = { index ->
                        navController.navigate("${AppRoute.ChessStatisticsDetail}/$index")
                    },
                    onGomokuRecordClick = { index ->
                        navController.navigate("${AppRoute.GomokuStatisticsDetail}/$index")
                    },
                    onGoRecordClick = { index ->
                        navController.navigate("${AppRoute.GoStatisticsDetail}/$index")
                    },
                    onInternationalChessRecordClick = { index ->
                        navController.navigate("${AppRoute.InternationalChessStatisticsDetail}/$index")
                    },
                )
            }
            sudokuDestinations(navController, session, returnToMain, darkTheme,
                onPaletteChange = { palette ->
                    session.palette = palette
                    if (palette == GamePalette.Black) appearance.mode = ThemeMode.Dark
                    else if (darkTheme) appearance.mode = ThemeMode.Light
                })
            chessDestinations(navController, chessSession, returnToMain)
            gomokuDestinations(navController, gomokuSession, returnToMain)
            goDestinations(navController, goSession, returnToMain)
            game2048Destinations(navController, game2048Session, returnToMain)
            sokobanDestinations(navController, sokobanSession, returnToMain)
            klotskiDestinations(navController, klotskiSession, returnToMain)
            internationalChessDestinations(navController, internationalChessSession, returnToMain)
            tetrisDestinations(navController, tetrisSession, tetrisMatch, returnToMain)
            mazeDestinations(navController, mazeSession, returnToMain)
            minesweeperDestinations(navController, minesweeperSession, returnToMain)
        }
    }
}
