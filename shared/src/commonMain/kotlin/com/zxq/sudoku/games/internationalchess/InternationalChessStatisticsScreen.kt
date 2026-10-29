package com.zxq.sudoku.games.internationalchess

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
internal fun InternationalChessStatisticsScreen(
    statistics: InternationalChessStatistics,
    onRecordClick: (Int) -> Unit,
) {
    val records = statistics.records.withIndex().sortedByDescending { it.value.startedMillis }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("国际象棋 · 对局记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("已完成 ${records.size} 局 · 白胜 ${records.count { it.value.result == "白方获胜" }} 局 · 黑胜 ${records.count { it.value.result == "黑方获胜" }} 局 · 和棋 ${records.count { it.value.result.startsWith("和棋") }} 局",
            style = MaterialTheme.typography.bodyMedium)
        if (records.isEmpty()) Text("暂无对局记录", style = MaterialTheme.typography.bodyLarge)
        records.forEach { (index, record) ->
            Card(Modifier.fillMaxWidth().clickable { onRecordClick(index) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(record.modeLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(record.result, style = MaterialTheme.typography.labelLarge)
                    Text("${record.startedAt} · 用时 ${chessDuration(record.elapsedSeconds)} · 共 ${record.totalMoves} 步",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun InternationalChessStatisticsDetailScreen(record: InternationalChessRecord?, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()
        .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回统计")
            }
            Text("国际象棋 · 对局详情", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (record == null) {
            Text("对局记录不存在", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(record.modeLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(record.result, style = MaterialTheme.typography.titleMedium)
                Text("开始 ${record.startedAt}")
                Text("结束 ${record.endedAt}")
                Text("用时 ${chessDuration(record.elapsedSeconds)}")
                Text("共 ${record.totalMoves} 步 · 白 ${record.whiteMoves} 步 · 黑 ${record.blackMoves} 步")
            }
        }
        Text("最终棋盘", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.widthIn(max = 340.dp).fillMaxWidth()) {
                InternationalChessBoard(InternationalChessBoardCode.decode(record.finalBoard).orEmpty(),
                    lastMove = null, selected = null, legalTargets = emptySet(), enabled = false, onSquare = {})
            }
        }
    }
}
