package com.zxq.sudoku.games.tetris

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
internal fun TetrisStatisticsScreen(statistics: TetrisStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("俄罗斯方块 · 记录", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("最高分 ${statistics.bestScore}", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black)
                Text("单局最多消除 ${statistics.bestLines} 行 · 最高等级 ${statistics.bestLevel}")
                Text("已开 ${statistics.played} 局 · 已结束 ${statistics.finished} 局 · 累计消除 ${statistics.totalLines} 行")
                if (statistics.bestAt.isNotEmpty()) Text("最高分刷新于 ${statistics.bestAt}",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
