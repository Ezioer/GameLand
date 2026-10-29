package com.zxq.sudoku.games.minesweeper

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
internal fun MinesweeperStatisticsScreen(statistics: MinesweeperStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("扫雷 · 游玩记录", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text("已开 ${statistics.records.sumOf { it.played }} 局 · 胜利 ${statistics.records.sumOf { it.won }} 局",
            style = MaterialTheme.typography.bodyMedium)
        MinesweeperDifficulty.entries.forEach { difficulty ->
            val record = statistics.forDifficulty(difficulty)
            Card(Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${difficulty.label} · ${difficulty.columns} × ${difficulty.rows}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${record.won}/${record.played}", style = MaterialTheme.typography.labelLarge)
                    }
                    Text("胜利 ${record.won} 局 · 失败 ${record.lost} 局",
                        style = MaterialTheme.typography.bodyMedium)
                    if (record.bestSeconds != null) {
                        Text("最快 ${formatMinesweeperTime(record.bestSeconds)}",
                            style = MaterialTheme.typography.bodyMedium)
                        if (record.bestAt.isNotEmpty()) Text("刷新于 ${record.bestAt}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
