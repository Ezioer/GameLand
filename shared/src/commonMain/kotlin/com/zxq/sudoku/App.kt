package com.zxq.sudoku

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.zxq.sudoku.games.chess.rememberChessSession
import com.zxq.sudoku.games.gomoku.rememberGomokuSession
import com.zxq.sudoku.games.go.rememberGoSession
import com.zxq.sudoku.games.game2048.rememberGame2048Session
import com.zxq.sudoku.games.sudoku.rememberSudokuSession
import com.zxq.sudoku.navigation.AppNavigation
import com.zxq.sudoku.theme.IslandTheme
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
    currentDate: () -> String = { Clock.System.now().toString().take(10) },
    currentDateTime: () -> String = { Clock.System.now().toString().take(19).replace('T', ' ') + " UTC" },
) {
    IslandTheme {
        val sudokuSession = rememberSudokuSession(
            initialBeginnerTutorialSeen = initialBeginnerTutorialSeen,
            onBeginnerTutorialSeen = onBeginnerTutorialSeen,
            initialNoteTutorialSeen = initialNoteTutorialSeen,
            onNoteTutorialSeen = onNoteTutorialSeen,
            initialStatistics = initialStatistics,
            onStatisticsChange = onStatisticsChange,
            currentDate = currentDate,
        )
        val chessSession = rememberChessSession(initialChessStatistics, onChessStatisticsChange, currentDateTime)
        val gomokuSession = rememberGomokuSession(initialGomokuStatistics, onGomokuStatisticsChange, currentDateTime)
        val goSession = rememberGoSession(initialGoStatistics, onGoStatisticsChange, currentDateTime)
        val game2048Session = rememberGame2048Session(initialGame2048Statistics,
            onGame2048StatisticsChange, initialGame2048Progress,
            onGame2048ProgressChange, currentDateTime)
        AppNavigation(sudokuSession, chessSession, gomokuSession, goSession, game2048Session)
    }
}

@Preview
@Composable
private fun AppPreview() {
    App()
}
