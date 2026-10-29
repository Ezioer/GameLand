package com.zxq.sudoku.games.maze

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun MazeStatisticsScreen(statistics: MazeStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("迷宫 · 游玩记录", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text("已开 ${statistics.records.sumOf { it.played }} 局 · 通关 ${statistics.records.sumOf { it.completed }} 次",
            style = MaterialTheme.typography.bodyMedium)
        MazeDifficulty.entries.forEach { difficulty ->
            val record = statistics.forDifficulty(difficulty)
            Card(Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${difficulty.label} · ${difficulty.size} × ${difficulty.size}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${record.completed}/${record.played}", style = MaterialTheme.typography.labelLarge)
                    }
                    if (record.bestMoves == null) {
                        Text("尚未通关", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("最佳 ${record.bestMoves} 步 · ${formatMazeTime(record.bestSeconds ?: 0)} · ${record.bestHints ?: 0} 次提示",
                            style = MaterialTheme.typography.bodyMedium)
                        if (record.bestAt.isNotEmpty()) Text("刷新于 ${record.bestAt}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
