package com.zxq.sudoku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.tooling.preview.Preview
import com.zxq.sudoku.collection.CollectionGame
import com.zxq.sudoku.collection.rememberCollectionPlaySession
import com.zxq.sudoku.games.chess.rememberChessSession
import com.zxq.sudoku.games.gomoku.rememberGomokuSession
import com.zxq.sudoku.games.go.rememberGoSession
import com.zxq.sudoku.games.game2048.rememberGame2048Session
import com.zxq.sudoku.games.sokoban.rememberSokobanSession
import com.zxq.sudoku.games.klotski.rememberKlotskiSession
import com.zxq.sudoku.games.internationalchess.rememberInternationalChessSession
import com.zxq.sudoku.games.tetris.rememberTetrisSession
import com.zxq.sudoku.games.tetris.MatchTransport
import com.zxq.sudoku.games.tetris.UnavailableMatchTransport
import com.zxq.sudoku.games.tetris.rememberTetrisMatch
import com.zxq.sudoku.games.maze.rememberMazeSession
import com.zxq.sudoku.games.minesweeper.rememberMinesweeperSession
import com.zxq.sudoku.games.sudoku.rememberSudokuSession
import com.zxq.sudoku.navigation.AppNavigation
import com.zxq.sudoku.theme.IslandTheme
import com.zxq.sudoku.theme.darkThemeEnabled
import com.zxq.sudoku.theme.rememberAppAppearance
import kotlin.time.Clock

@Composable
fun App(
    initialBeginnerTutorialSeen: Boolean = false,
    onBeginnerTutorialSeen: () -> Unit = {},
    initialNoteTutorialSeen: Boolean = false,
    onNoteTutorialSeen: () -> Unit = {},
    initialStatistics: String = "",
    onStatisticsChange: (String) -> Unit = {},
    initialChessStatistics: String = "",
    onChessStatisticsChange: (String) -> Unit = {},
    initialGomokuStatistics: String = "",
    onGomokuStatisticsChange: (String) -> Unit = {},
    initialGoStatistics: String = "",
    onGoStatisticsChange: (String) -> Unit = {},
    initialGame2048Statistics: String = "",
    onGame2048StatisticsChange: (String) -> Unit = {},
    initialGame2048Progress: String = "",
    onGame2048ProgressChange: (String) -> Unit = {},
    initialSokobanProgress: String = "",
    onSokobanProgressChange: (String) -> Unit = {},
    initialSokobanStatistics: String = "",
    onSokobanStatisticsChange: (String) -> Unit = {},
    initialKlotskiProgress: String = "",
    onKlotskiProgressChange: (String) -> Unit = {},
    initialKlotskiStatistics: String = "",
    onKlotskiStatisticsChange: (String) -> Unit = {},
    initialInternationalChessStatistics: String = "",
    onInternationalChessStatisticsChange: (String) -> Unit = {},
    initialTetrisProgress: String = "",
    onTetrisProgressChange: (String) -> Unit = {},
    initialTetrisStatistics: String = "",
    onTetrisStatisticsChange: (String) -> Unit = {},
    initialMazeProgress: String = "",
    onMazeProgressChange: (String) -> Unit = {},
    initialMazeStatistics: String = "",
    onMazeStatisticsChange: (String) -> Unit = {},
    initialMinesweeperProgress: String = "",
    onMinesweeperProgressChange: (String) -> Unit = {},
    initialMinesweeperStatistics: String = "",
    onMinesweeperStatisticsChange: (String) -> Unit = {},
    initialCollectionPlayStatistics: String = "",
    onCollectionPlayStatisticsChange: (String) -> Unit = {},
    isAppForeground: Boolean = true,
    initialThemeMode: String = "",
    onThemeModeChange: (String) -> Unit = {},
    initialBackgroundPalette: String = "",
    onBackgroundPaletteChange: (String) -> Unit = {},
    onDarkThemeChange: (Boolean) -> Unit = {},
    matchTransport: MatchTransport = UnavailableMatchTransport,
    currentDate: () -> String = { Clock.System.now().toString().take(10) },
    currentDateTime: () -> String = { Clock.System.now().toString().take(19).replace('T', ' ') + " UTC" },
    onCyclingWebClick: (() -> Unit)? = null,
) {
    val appearance = rememberAppAppearance(initialThemeMode, onThemeModeChange,
        initialBackgroundPalette, onBackgroundPaletteChange)
    val dark = darkThemeEnabled(appearance.mode, appearance.palette)
    SideEffect { onDarkThemeChange(dark) }
    IslandTheme(dark, appearance.palette) {
        val playSession = rememberCollectionPlaySession(initialCollectionPlayStatistics,
            onCollectionPlayStatisticsChange)
        val sudokuSession = rememberSudokuSession(
            initialBeginnerTutorialSeen = initialBeginnerTutorialSeen,
            onBeginnerTutorialSeen = onBeginnerTutorialSeen,
            initialNoteTutorialSeen = initialNoteTutorialSeen,
            onNoteTutorialSeen = onNoteTutorialSeen,
            initialStatistics = initialStatistics,
            onStatisticsChange = onStatisticsChange,
            currentDate = currentDate,
            onStart = { playSession.started(CollectionGame.Sudoku) },
            sharedPaletteState = appearance.paletteState,
            onGamePaletteChange = { appearance.palette = it },
        )
        val chessSession = rememberChessSession(initialChessStatistics, onChessStatisticsChange,
            currentDateTime, onStart = { playSession.started(CollectionGame.ChineseChess) })
        val gomokuSession = rememberGomokuSession(initialGomokuStatistics, onGomokuStatisticsChange,
            currentDateTime, onStart = { playSession.started(CollectionGame.Gomoku) })
        val goSession = rememberGoSession(initialGoStatistics, onGoStatisticsChange,
            currentDateTime, onStart = { playSession.started(CollectionGame.Go) })
        val game2048Session = rememberGame2048Session(initialGame2048Statistics,
            onGame2048StatisticsChange, initialGame2048Progress,
            onGame2048ProgressChange, currentDateTime,
            onStart = { playSession.started(CollectionGame.Game2048) })
        val sokobanSession = rememberSokobanSession(initialSokobanProgress,
            onSokobanProgressChange, initialSokobanStatistics,
            onSokobanStatisticsChange, currentDateTime,
            onStart = { playSession.started(CollectionGame.Sokoban) })
        val klotskiSession = rememberKlotskiSession(initialKlotskiProgress,
            onKlotskiProgressChange, initialKlotskiStatistics,
            onKlotskiStatisticsChange, currentDateTime,
            onStart = { playSession.started(CollectionGame.Klotski) })
        val internationalChessSession = rememberInternationalChessSession(
            initialInternationalChessStatistics, onInternationalChessStatisticsChange, currentDateTime,
            onStart = { playSession.started(CollectionGame.InternationalChess) })
        val tetrisSession = rememberTetrisSession(initialTetrisProgress, onTetrisProgressChange,
            initialTetrisStatistics, onTetrisStatisticsChange, currentDateTime,
            onStart = { playSession.started(CollectionGame.Tetris) })
        val tetrisMatch = rememberTetrisMatch(matchTransport)
        val mazeSession = rememberMazeSession(initialMazeProgress, onMazeProgressChange,
            initialMazeStatistics, onMazeStatisticsChange, currentDateTime,
            onStart = { playSession.started(CollectionGame.Maze) })
        val minesweeperSession = rememberMinesweeperSession(initialMinesweeperProgress,
            onMinesweeperProgressChange, initialMinesweeperStatistics,
            onMinesweeperStatisticsChange, currentDateTime,
            onStart = { playSession.started(CollectionGame.Minesweeper) })
        AppNavigation(sudokuSession, chessSession, gomokuSession, goSession,
            game2048Session, sokobanSession, klotskiSession, internationalChessSession, tetrisSession,
            mazeSession, minesweeperSession, playSession, isAppForeground, appearance, dark,
            tetrisMatch, onCyclingWebClick = onCyclingWebClick)
    }
}

@Preview
@Composable
private fun AppPreview() {
    App()
}
