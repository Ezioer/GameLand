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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zxq.sudoku.collection.CollectionHomeRoute
import com.zxq.sudoku.collection.CollectionStatisticsScreen
import com.zxq.sudoku.games.chess.chessDestinations
import com.zxq.sudoku.games.chess.ChessSession
import com.zxq.sudoku.games.gomoku.GomokuSession
import com.zxq.sudoku.games.gomoku.gomokuDestinations
import com.zxq.sudoku.games.go.GoSession
import com.zxq.sudoku.games.go.goDestinations
import com.zxq.sudoku.games.game2048.Game2048Session
import com.zxq.sudoku.games.game2048.game2048Destinations
import com.zxq.sudoku.games.sudoku.SudokuSession
import com.zxq.sudoku.games.sudoku.sudokuDestinations
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal object AppRoute {
    const val Home = "home"
    const val Statistics = "statistics"
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
internal fun AppNavigation(
    session: SudokuSession,
    chessSession: ChessSession,
    gomokuSession: GomokuSession,
    goSession: GoSession,
    game2048Session: Game2048Session,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: AppRoute.Home
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
            startDestination = AppRoute.Home,
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
            composable(AppRoute.Home) {
                CollectionHomeRoute(
                    statistics = session.statistics,
                    onSudokuClick = { navController.navigate(AppRoute.SudokuIsland) },
                    onChessClick = { navController.navigate(AppRoute.ChessIsland) },
                    onGomokuClick = { navController.navigate(AppRoute.GomokuIsland) },
                    onGoClick = { navController.navigate(AppRoute.GoIsland) },
                    onGame2048Click = { navController.navigate(AppRoute.Game2048Island) },
                    onStatisticsClick = { navController.navigate(AppRoute.Statistics) },
                )
            }
            composable(AppRoute.Statistics) {
                CollectionStatisticsScreen(
                    session.statistics,
                    chessSession.statistics,
                    gomokuSession.statistics,
                    goSession.statistics,
                    game2048Session.statistics,
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
                )
            }
            sudokuDestinations(navController, session, returnToMain)
            chessDestinations(navController, chessSession, returnToMain)
            gomokuDestinations(navController, gomokuSession, returnToMain)
            goDestinations(navController, goSession, returnToMain)
            game2048Destinations(navController, game2048Session, returnToMain)
        }
    }
}
