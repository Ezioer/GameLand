package com.zxq.sudoku.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.games.chess.ChessStatistics
import com.zxq.sudoku.games.chess.ChessStatisticsScreen
import com.zxq.sudoku.games.gomoku.GomokuStatistics
import com.zxq.sudoku.games.gomoku.GomokuStatisticsScreen
import com.zxq.sudoku.games.go.GoStatistics
import com.zxq.sudoku.games.go.GoStatisticsScreen
import com.zxq.sudoku.games.game2048.Game2048Statistics
import com.zxq.sudoku.games.game2048.Game2048StatisticsScreen
import com.zxq.sudoku.games.sokoban.SokobanStatistics
import com.zxq.sudoku.games.sokoban.SokobanStatisticsScreen
import com.zxq.sudoku.games.klotski.KlotskiStatistics
import com.zxq.sudoku.games.klotski.KlotskiStatisticsScreen
import com.zxq.sudoku.games.internationalchess.InternationalChessStatistics
import com.zxq.sudoku.games.internationalchess.InternationalChessStatisticsScreen
import com.zxq.sudoku.games.tetris.TetrisStatistics
import com.zxq.sudoku.games.tetris.TetrisStatisticsScreen
import com.zxq.sudoku.games.maze.MazeStatistics
import com.zxq.sudoku.games.maze.MazeStatisticsScreen
import com.zxq.sudoku.games.minesweeper.MinesweeperStatistics
import com.zxq.sudoku.games.minesweeper.MinesweeperStatisticsScreen
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
    sokobanStatistics: SokobanStatistics,
    klotskiStatistics: KlotskiStatistics,
    internationalChessStatistics: InternationalChessStatistics,
    tetrisStatistics: TetrisStatistics,
    mazeStatistics: MazeStatistics,
    minesweeperStatistics: MinesweeperStatistics,
    playStatistics: CollectionPlayStatistics,
    onSudokuDifficultyClick: (SudokuDifficulty) -> Unit,
    onChessRecordClick: (Int) -> Unit,
    onGomokuRecordClick: (Int) -> Unit,
    onGoRecordClick: (Int) -> Unit,
    onInternationalChessRecordClick: (Int) -> Unit,
) {
    var selectedGame by rememberSaveable { mutableStateOf(CollectionGame.Sudoku) }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(top = 46.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("游玩统计", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                "游玩时长", formatPlayDuration(playStatistics.totalSeconds),
                Modifier.weight(1f), MaterialTheme.colorScheme.primaryContainer
            )
            SummaryCard(
                "游玩关卡", "${playStatistics.totalRounds} 局",
                Modifier.weight(1f), MaterialTheme.colorScheme.secondaryContainer
            )
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)) {
            Spacer(Modifier.width(16.dp))
            CollectionGame.entries.forEach { item ->
                FilterChip(
                    leadingIcon = if (selectedGame == item) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    },
                    selected = selectedGame == item,
                    onClick = { selectedGame = item },
                    label = { Text(item.label) },
                )
            }
        }
        val selectedTotals = playStatistics.forGame(selectedGame)
        Column(
            modifier = Modifier.fillMaxSize().clip(shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 15.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    "${selectedGame.label} · 局数", "${selectedTotals.rounds} 局",
                    Modifier.weight(1f), MaterialTheme.colorScheme.surfaceContainer
                )
                SummaryCard(
                    "${selectedGame.label} · 时长", formatPlayDuration(selectedTotals.seconds),
                    Modifier.weight(1f), MaterialTheme.colorScheme.surfaceContainer
                )
            }
            Spacer(Modifier.height(10.dp))
            when (selectedGame) {
                CollectionGame.Sudoku -> SudokuStatisticsScreen(statistics, onSudokuDifficultyClick)
                CollectionGame.ChineseChess -> ChessStatisticsScreen(chessStatistics, onChessRecordClick)
                CollectionGame.Gomoku -> GomokuStatisticsScreen(gomokuStatistics, onGomokuRecordClick)
                CollectionGame.Go -> GoStatisticsScreen(goStatistics, onGoRecordClick)
                CollectionGame.Game2048 -> Game2048StatisticsScreen(game2048Statistics)
                CollectionGame.Sokoban -> SokobanStatisticsScreen(sokobanStatistics)
                CollectionGame.Klotski -> KlotskiStatisticsScreen(klotskiStatistics)
                CollectionGame.InternationalChess -> InternationalChessStatisticsScreen(
                    internationalChessStatistics, onInternationalChessRecordClick
                )

                CollectionGame.Tetris -> TetrisStatisticsScreen(tetrisStatistics)
                CollectionGame.Maze -> MazeStatisticsScreen(mazeStatistics)
                CollectionGame.Minesweeper -> MinesweeperStatisticsScreen(minesweeperStatistics)
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
}

@Composable
private fun SummaryCard(
    label: String, value: String, modifier: Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier, shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Ink)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}
