package com.zxq.sudoku.games.sudoku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SudokuStatisticsScreen(statistics: GameStatistics, onDifficultyClick: (SudokuDifficulty) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("数独 · 各难度", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        SudokuDifficulty.entries.forEach { item ->
            val record = statistics.forDifficulty(item)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onDifficultyClick(item) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("游玩 ${record.played} 局 · 成功 ${record.wins} 局 · 失败 ${record.losses} 局")
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun SudokuStatisticsDetailScreen(
    statistics: GameStatistics,
    difficulty: SudokuDifficulty?,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回统计")
            }
            Text("${difficulty?.label ?: "数独"} · 对局记录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (difficulty == null) {
            Text("难度不存在", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }
        val games = statistics.gamesFor(difficulty).asReversed().sortedByDescending { it.date }
        if (games.isEmpty()) {
            Text(
                if (statistics.forDifficulty(difficulty).played > 0) "旧版记录只保存了汇总，后续对局会显示在这里。" else "暂无逐局记录",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        var lastDate: String? = null
        games.forEach { game ->
            val date = game.date.ifEmpty { "日期未知" }
            if (date != lastDate) {
                Text(date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                lastDate = date
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        when (game.result) {
                            GameResult.InProgress -> "进行中"
                            GameResult.Left -> "未完成"
                            GameResult.Won -> "成功"
                            GameResult.Lost -> "失败"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                    Text("耗时 ${formatTime(game.elapsedSeconds)} · 错误 ${game.mistakes} 次")
                }
            }
        }
    }
}
