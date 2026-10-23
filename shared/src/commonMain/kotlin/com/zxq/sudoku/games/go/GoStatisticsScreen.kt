package com.zxq.sudoku.games.go

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
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
internal fun GoStatisticsScreen(statistics: GoStatistics, onRecordClick: (Int) -> Unit) {
    val records = statistics.records.withIndex().sortedByDescending { it.value.startedMillis }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("围棋 · 对局记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("已完成 ${records.size} 局 · 黑胜 ${statistics.records.count { it.result == GoResult.BlackWon }} 局 · 白胜 ${statistics.records.count { it.result == GoResult.WhiteWon }} 局 · 和棋 ${statistics.records.count { it.result == GoResult.Draw }} 局",
            style = MaterialTheme.typography.bodyMedium)
        if (records.isEmpty()) Text("暂无对局记录", style = MaterialTheme.typography.bodyLarge)
        records.forEach { (index, record) ->
            Card(Modifier.fillMaxWidth().clickable { onRecordClick(index) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(record.modeLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${record.resultLabel} · ${record.endingLabel}", style = MaterialTheme.typography.labelLarge)
                    Text("${record.startedAt} · 用时 ${formatGoDuration(record.elapsedSeconds)} · 共 ${record.totalMoves} 手",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun GoStatisticsDetailScreen(record: GoRecord?, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()
        .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回统计")
            }
            Text("围棋 · 对局详情", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
        }
        if (record == null) {
            Text("对局记录不存在", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }
        Card(Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(record.modeLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${record.resultLabel} · ${record.endingLabel}", style = MaterialTheme.typography.titleMedium)
                Text("开始 ${record.startedAt}")
                Text("结束 ${record.endedAt}")
                Text("用时 ${formatGoDuration(record.elapsedSeconds)} · 共 ${record.totalMoves} 手")
                Text("提子：黑 ${record.blackCaptures} · 白 ${record.whiteCaptures}")
                if (record.blackArea != null && record.whiteArea != null)
                    Text("数子：黑 ${record.blackArea} · 白 ${record.whiteArea}+5.5")
            }
        }
        Text("最终棋盘", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.widthIn(max = 340.dp).fillMaxWidth()) {
                GoBoard(GoBoardCode.decode(record.finalBoard) ?: List(GoCells) { null },
                    lastMove = null, enabled = false, onPoint = {})
            }
        }
    }
}

internal fun formatGoDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = seconds % 3600 / 60
    val remaining = seconds % 60
    return if (hours > 0) "${hours}时${minutes}分${remaining}秒" else "${minutes}分${remaining}秒"
}
