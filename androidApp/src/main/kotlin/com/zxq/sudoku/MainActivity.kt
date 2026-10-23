package com.zxq.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settings = getSharedPreferences("sudoku_settings", MODE_PRIVATE)

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
