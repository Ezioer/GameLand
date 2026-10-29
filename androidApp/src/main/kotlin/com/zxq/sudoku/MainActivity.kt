package com.zxq.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private var appForeground by mutableStateOf(false)

    override fun onStart() {
        super.onStart()
        appForeground = true
    }

    override fun onStop() {
        appForeground = false
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settings = getSharedPreferences("sudoku_settings", MODE_PRIVATE)
        val matchTransport = AndroidNearbyMatchTransport(this)

        setContent {
            App(
                initialBeginnerTutorialSeen = settings.getBoolean("beginner_tutorial_seen", false),
                onBeginnerTutorialSeen = {
                    settings.edit().putBoolean("beginner_tutorial_seen", true).apply()
                },
                initialNoteTutorialSeen = settings.getBoolean("note_tutorial_seen", false),
                onNoteTutorialSeen = {
                    settings.edit().putBoolean("note_tutorial_seen", true).apply()
                },
                initialStatistics = settings.getString("game_statistics", null).orEmpty(),
                onStatisticsChange = { value ->
                    settings.edit().putString("game_statistics", value).apply()
                },
                initialChessStatistics = settings.getString("chess_statistics", null).orEmpty(),
                onChessStatisticsChange = { value ->
                    settings.edit().putString("chess_statistics", value).apply()
                },
                initialGomokuStatistics = settings.getString("gomoku_statistics", null).orEmpty(),
                onGomokuStatisticsChange = { value ->
                    settings.edit().putString("gomoku_statistics", value).apply()
                },
                initialGoStatistics = settings.getString("go_statistics", null).orEmpty(),
                onGoStatisticsChange = { value ->
                    settings.edit().putString("go_statistics", value).apply()
                },
                initialGame2048Statistics = settings.getString("game2048_statistics", null).orEmpty(),
                onGame2048StatisticsChange = { value ->
                    settings.edit().putString("game2048_statistics", value).apply()
                },
                initialGame2048Progress = settings.getString("game2048_progress", null).orEmpty(),
                onGame2048ProgressChange = { value ->
                    settings.edit().putString("game2048_progress", value).apply()
                },
                initialSokobanProgress = settings.getString("sokoban_progress", null).orEmpty(),
                onSokobanProgressChange = { value ->
                    settings.edit().putString("sokoban_progress", value).apply()
                },
                initialSokobanStatistics = settings.getString("sokoban_statistics", null).orEmpty(),
                onSokobanStatisticsChange = { value ->
                    settings.edit().putString("sokoban_statistics", value).apply()
                },
                initialKlotskiProgress = settings.getString("klotski_progress", null).orEmpty(),
                onKlotskiProgressChange = { value ->
                    settings.edit().putString("klotski_progress", value).apply()
                },
                initialKlotskiStatistics = settings.getString("klotski_statistics", null).orEmpty(),
                onKlotskiStatisticsChange = { value ->
                    settings.edit().putString("klotski_statistics", value).apply()
                },
                initialInternationalChessStatistics = settings.getString("international_chess_statistics", null).orEmpty(),
                onInternationalChessStatisticsChange = { value ->
                    settings.edit().putString("international_chess_statistics", value).apply()
                },
                initialTetrisProgress = settings.getString("tetris_progress", null).orEmpty(),
                onTetrisProgressChange = { value ->
                    settings.edit().putString("tetris_progress", value).apply()
                },
                initialTetrisStatistics = settings.getString("tetris_statistics", null).orEmpty(),
                onTetrisStatisticsChange = { value ->
                    settings.edit().putString("tetris_statistics", value).apply()
                },
                initialMazeProgress = settings.getString("maze_progress", null).orEmpty(),
                onMazeProgressChange = { value ->
                    settings.edit().putString("maze_progress", value).apply()
                },
                initialMazeStatistics = settings.getString("maze_statistics", null).orEmpty(),
                onMazeStatisticsChange = { value ->
                    settings.edit().putString("maze_statistics", value).apply()
                },
                initialMinesweeperProgress = settings.getString("minesweeper_progress", null).orEmpty(),
                onMinesweeperProgressChange = { value ->
                    settings.edit().putString("minesweeper_progress", value).apply()
                },
                initialMinesweeperStatistics = settings.getString("minesweeper_statistics", null).orEmpty(),
                onMinesweeperStatisticsChange = { value ->
                    settings.edit().putString("minesweeper_statistics", value).apply()
                },
                initialCollectionPlayStatistics = settings.getString("collection_play_statistics", null).orEmpty(),
                onCollectionPlayStatisticsChange = { value ->
                    settings.edit().putString("collection_play_statistics", value).apply()
                },
                isAppForeground = appForeground,
                initialThemeMode = settings.getString("theme_mode", null).orEmpty(),
                onThemeModeChange = { value ->
                    settings.edit().putString("theme_mode", value).apply()
                },
                initialBackgroundPalette = settings.getString("background_palette", null).orEmpty(),
                onBackgroundPaletteChange = { value ->
                    settings.edit().putString("background_palette", value).apply()
                },
                onDarkThemeChange = { dark ->
                    WindowCompat.getInsetsController(window, window.decorView)
                        .isAppearanceLightStatusBars = !dark
                },
                matchTransport = matchTransport,
                currentDate = { LocalDate.now().toString() },
                currentDateTime = { LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) },
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
