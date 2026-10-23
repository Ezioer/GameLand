package com.zxq.sudoku.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxq.sudoku.games.chess.ChessStatistics
import com.zxq.sudoku.games.chess.ChessStatisticsScreen
import com.zxq.sudoku.games.gomoku.GomokuStatistics
import com.zxq.sudoku.games.gomoku.GomokuStatisticsScreen
import com.zxq.sudoku.games.go.GoStatistics
import com.zxq.sudoku.games.go.GoStatisticsScreen
import com.zxq.sudoku.games.game2048.Game2048Statistics
import com.zxq.sudoku.games.game2048.Game2048StatisticsScreen
import com.zxq.sudoku.games.sudoku.GameStatistics
import com.zxq.sudoku.games.sudoku.SudokuStatisticsScreen
import com.zxq.sudoku.games.sudoku.SudokuDifficulty

@Composable
internal fun CollectionStatisticsScreen(
    statistics: GameStatistics,
    chessStatistics: ChessStatistics,
    gomokuStatistics: GomokuStatistics,
    goStatistics: GoStatistics,
    game2048Statistics: Game2048Statistics,
    onSudokuDifficultyClick: (SudokuDifficulty) -> Unit,
    onChessRecordClick: (Int) -> Unit,
    onGomokuRecordClick: (Int) -> Unit,
    onGoRecordClick: (Int) -> Unit,
) {
    var selectedGame by rememberSaveable { mutableStateOf(CollectionGame.Sudoku) }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 46.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("游玩统计", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CollectionGame.entries.forEach { item ->
                FilterChip(
                    selected = selectedGame == item,
                    onClick = { selectedGame = item },
                    label = { Text(item.label) },
                )
            }
        }
        when (selectedGame) {
            CollectionGame.Sudoku -> SudokuStatisticsScreen(statistics, onSudokuDifficultyClick)
            CollectionGame.ChineseChess -> ChessStatisticsScreen(chessStatistics, onChessRecordClick)
            CollectionGame.Gomoku -> GomokuStatisticsScreen(gomokuStatistics, onGomokuRecordClick)
            CollectionGame.Go -> GoStatisticsScreen(goStatistics, onGoRecordClick)
            CollectionGame.Game2048 -> Game2048StatisticsScreen(game2048Statistics)
            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(selectedGame.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("请等待，游戏开放后将在这里展示游玩统计。", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
