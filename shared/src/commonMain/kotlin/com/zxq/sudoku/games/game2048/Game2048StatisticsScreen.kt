package com.zxq.sudoku.games.game2048

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
internal fun Game2048StatisticsScreen(statistics: Game2048Statistics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("2048 · 游玩记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("最高分 ${statistics.bestScore}", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black)
                Text("最大方块 ${statistics.bestTile}")
                if (statistics.bestAt.isNotEmpty()) Text("刷新于 ${statistics.bestAt}")
                Text("已开 ${statistics.played} 局 · 无路可走 ${statistics.completed} 局 · 达成 2048 ${statistics.wins} 次")
            }
        }
    }
}
