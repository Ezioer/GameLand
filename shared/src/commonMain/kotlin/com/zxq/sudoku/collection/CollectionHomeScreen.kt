package com.zxq.sudoku.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zxq.sudoku.games.sudoku.GameStatistics
import com.zxq.sudoku.games.sudoku.SudokuDifficulty
import com.zxq.sudoku.theme.IslandGreen
import com.zxq.sudoku.theme.Ink

@Composable
internal fun CollectionHomeRoute(
    statistics: GameStatistics,
    onBack: () -> Unit,
    onSudokuClick: () -> Unit,
    onChessClick: () -> Unit,
    onGomokuClick: () -> Unit,
    onGoClick: () -> Unit,
    onGame2048Click: () -> Unit,
    onSokobanClick: () -> Unit,
    onKlotskiClick: () -> Unit,
    onInternationalChessClick: () -> Unit,
    onTetrisClick: () -> Unit,
    onMazeClick: () -> Unit,
    onMinesweeperClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var pendingGame by rememberSaveable { mutableStateOf<CollectionGame?>(null) }
    CollectionHomeScreen(
        statistics = statistics,
        onBack = onBack,
        onGameClick = { game ->
            when (game) {
                CollectionGame.Sudoku -> onSudokuClick()
                CollectionGame.ChineseChess -> onChessClick()
                CollectionGame.Gomoku -> onGomokuClick()
                CollectionGame.Go -> onGoClick()
                CollectionGame.Game2048 -> onGame2048Click()
                CollectionGame.Sokoban -> onSokobanClick()
                CollectionGame.Klotski -> onKlotskiClick()
                CollectionGame.InternationalChess -> onInternationalChessClick()
                CollectionGame.Tetris -> onTetrisClick()
                CollectionGame.Maze -> onMazeClick()
                CollectionGame.Minesweeper -> onMinesweeperClick()
                else -> pendingGame = game
            }
        },
        onStatisticsClick = onStatisticsClick,
        onSettingsClick = onSettingsClick,
    )
    pendingGame?.let { game ->
        AlertDialog(
            onDismissRequest = { pendingGame = null },
            title = { Text(game.label) },
            text = { Text("请等待，${game.label}正在准备中。") },
            confirmButton = { TextButton(onClick = { pendingGame = null }) { Text("知道了") } },
        )
    }
}

@Composable
internal fun CollectionHomeScreen(
    statistics: GameStatistics,
    onBack: () -> Unit,
    onGameClick: (CollectionGame) -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val played = SudokuDifficulty.entries.sumOf { statistics.forDifficulty(it).played }
    val wins = SudokuDifficulty.entries.sumOf { statistics.forDifficulty(it).wins }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val columns = if (maxWidth >= 720.dp) 3 else 2
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 46.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 900.dp).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回我的项目")
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("益智小岛", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold)
                        Text("挑一个喜欢的游戏，慢慢玩。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    FilledTonalIconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "设置")
                    }
                }
                Spacer(Modifier.height(28.dp))
              /*  Text("游戏乐园", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))*/
                CollectionGame.entries.chunked(columns).forEach { rowGames ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowGames.forEach { item ->
                            CollectionGameCard(item, Modifier.weight(1f)) { onGameClick(item) }
                        }
                        repeat(columns - rowGames.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(12.dp))
                }
//               /* Text("游玩统计", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
//                Spacer(Modifier.height(12.dp))
//                Card(
//                    modifier = Modifier.fillMaxWidth().clickable(onClick = onStatisticsClick),
//                    shape = RoundedCornerShape(24.dp),
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//                ) {
//                    Row(
//                        Modifier.fillMaxWidth().padding(20.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically,
//                    ) {
//                        Column(Modifier.weight(1f)) {
//                            Text("数独小岛", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//                            Text("已玩 $played 局 · 成功 $wins 局", style = MaterialTheme.typography.bodyMedium)
//                        }
//                        Text("查看统计 ›", style = MaterialTheme.typography.labelLarge, color = IslandGreen)
//                    }
//                }
//                Spacer(Modifier.height(24.dp))*/
            }
        }
    }
}

@Composable
private fun CollectionGameCard(game: CollectionGame, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(164.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = game.color, contentColor = Ink),
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.65f)) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        game.badge, color = Ink, fontWeight = FontWeight.Black,
                        fontSize = if (game.badge.length > 2) 14.sp else 24.sp
                    )
                }
            }
            Column {
                Text(game.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(game.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

