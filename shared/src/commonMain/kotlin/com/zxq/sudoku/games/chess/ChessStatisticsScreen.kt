package com.zxq.sudoku.games.chess

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.zxq.sudoku.theme.Ink

@Composable
internal fun ChessStatisticsScreen(statistics: ChessStatistics, onRecordClick: (Int) -> Unit) {
    val completed = statistics.completedRecords
    val records = completed.withIndex().sortedByDescending { it.value.startedMillis }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("象棋 · 对局记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("已完成 ${records.size} 局 · 红胜 ${completed.count { it.result == ChessResult.RedWon }} 局 · 黑胜 ${completed.count { it.result == ChessResult.BlackWon }} 局 · 和棋 ${completed.count { it.result == ChessResult.Draw }} 局",
            style = MaterialTheme.typography.bodyMedium)
        if (records.isEmpty()) Text("暂无对局记录", style = MaterialTheme.typography.bodyLarge)
        records.forEach { (index, record) ->
            Card(Modifier.fillMaxWidth().clickable { onRecordClick(index) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(record.modeLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(chessResultLabel(record.result), style = MaterialTheme.typography.labelLarge)
                    Text("${record.startedAt} · 用时 ${formatChessDuration(record.elapsedSeconds)} · 共 ${record.totalMoves} 步",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ChessStatisticsDetailScreen(record: ChessRecord?, onBack: () -> Unit) {
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
            Text("象棋 · 对局详情", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (record == null) {
            Text("对局记录不存在", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(record.modeLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(chessResultLabel(record.result), style = MaterialTheme.typography.titleMedium)
                Text("开始 ${record.startedAt}")
                Text("结束 ${record.endedAt}")
                Text("用时 ${formatChessDuration(record.elapsedSeconds)}")
                Text("共 ${record.totalMoves} 步 · 红 ${record.redMoves} 步 · 黑 ${record.blackMoves} 步")
            }
        }
        Text("最终棋盘", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ChessBoardThumbnail(ChessBoardCode.decode(record.finalBoard).orEmpty(), 260.dp)
        }
    }
}

private fun chessResultLabel(result: ChessResult): String = when (result) {
    ChessResult.InProgress -> "进行中"
    ChessResult.Left -> "未完成"
    ChessResult.RedWon -> "红方获胜"
    ChessResult.BlackWon -> "黑方获胜"
    ChessResult.Draw -> "双方和棋"
}

internal fun formatChessDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = seconds % 3600 / 60
    val remaining = seconds % 60
    return if (hours > 0) "${hours}时${minutes}分${remaining}秒" else "${minutes}分${remaining}秒"
}

@Composable
private fun ChessBoardThumbnail(board: List<ChessPiece?>, width: Dp) {
    BoxWithConstraints(
        Modifier.width(width).aspectRatio(0.9f)
            .background(Color(0xFFF4DCA9), RoundedCornerShape(8.dp)),
    ) {
        val cell = maxWidth / 9
        Canvas(Modifier.matchParentSize()) {
            val unit = size.width / 9f
            val ink = Ink.copy(alpha = 0.55f)
            for (r in 0..9) {
                val y = (r + 0.5f) * unit
                drawLine(ink, Offset(unit / 2, y), Offset(size.width - unit / 2, y), 0.6.dp.toPx())
            }
            for (c in 0..8) {
                val x = (c + 0.5f) * unit
                if (c == 0 || c == 8) drawLine(ink, Offset(x, unit / 2), Offset(x, size.height - unit / 2), 0.6.dp.toPx())
                else {
                    drawLine(ink, Offset(x, unit / 2), Offset(x, 4.5f * unit), 0.6.dp.toPx())
                    drawLine(ink, Offset(x, 5.5f * unit), Offset(x, size.height - unit / 2), 0.6.dp.toPx())
                }
            }
        }
        board.forEachIndexed { point, piece ->
            if (piece != null) {
                Box(Modifier.offset(x = cell * (point % 9), y = cell * (point / 9)).size(cell),
                    contentAlignment = Alignment.Center) {
                    Surface(Modifier.size(cell * 0.86f), shape = CircleShape,
                        color = if (piece.side == ChessSide.Red) Color(0xFFFFF6E6) else Color(0xFFE9E5D6)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(piece.label, fontSize = (cell.value * 0.60f).sp,
                                fontWeight = FontWeight.Bold,
                                color = if (piece.side == ChessSide.Red) Color(0xFFB64B45) else Color(0xFF40504A))
                        }
                    }
                }
            }
        }
    }
}
