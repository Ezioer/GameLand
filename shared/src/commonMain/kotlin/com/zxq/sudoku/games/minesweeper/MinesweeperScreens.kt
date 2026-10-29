package com.zxq.sudoku.games.minesweeper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.delay

private val hiddenColor = Color(0xFFD5E7D4)
private val openedColor = Color(0xFFFFFAF0)
private val mineColor = Color(0xFFFFD2C9)
private val flagColor = Color(0xFFE7DDF7)

@Composable
internal fun MinesweeperIslandRoute(
    statistics: MinesweeperStatistics,
    savedGame: MinesweeperGame?,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onStart: (MinesweeperDifficulty) -> Unit,
) {
    var replay by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回游戏合集")
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("扫雷小岛", style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold)
                    Text("从数字里找线索，安全地揭开整片棋盘。", color = IslandGreen)
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("玩法教程", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge,
                            color = IslandGreen)
                        IconButton(onClick = { replay++ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "重播扫雷教程")
                        }
                    }
                    Text("数字代表周围八格的地雷数", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    MinesweeperTutorial(replay)
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onContinue, enabled = savedGame?.status == MinesweeperStatus.Playing,
                modifier = Modifier.fillMaxWidth()) {
                Text(if (savedGame == null) "继续" else "继续 · ${savedGame.difficulty.label}")
            }
            Spacer(Modifier.height(20.dp))
            Text("选择难度", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            MinesweeperDifficulty.entries.forEachIndexed { index, difficulty ->
                val record = statistics.forDifficulty(difficulty)
                Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    .clickable { onStart(difficulty) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = when (index) {
                        0 -> Color(0xFFE3EFD7)
                        1 -> Color(0xFFFFEBCF)
                        else -> Color(0xFFE6E5FA)
                    }, contentColor = Ink)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(difficulty.label, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("${difficulty.columns} × ${difficulty.rows} · ${difficulty.mineCount} 颗地雷",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${record.won}/${record.played}",
                            style = MaterialTheme.typography.labelLarge, color = IslandGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun MinesweeperTutorial(replay: Int) {
    var phase by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        phase = 0
        repeat(3) { delay(1_100); phase = it + 1 }
    }
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) { column ->
                        val index = row * 3 + column
                        val flagged = index == 2 && phase >= 2
                        val opened = index == 4 && phase >= 1 || phase >= 3 && index != 2
                        val color by animateColorAsState(
                            if (flagged) flagColor else if (opened) openedColor else hiddenColor,
                            animationSpec = tween(400), label = "扫雷教程格子")
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(color),
                            contentAlignment = Alignment.Center) {
                            val number = if (index in setOf(1, 4, 5)) "1" else ""
                            Text(if (flagged) "⚑" else if (opened) number else "",
                                fontWeight = FontWeight.Black, color = if (flagged) Ink else IslandGreen)
                        }
                    }
                }
                if (row < 2) Spacer(Modifier.height(3.dp))
            }
            Spacer(Modifier.height(10.dp))
            Crossfade(phase, animationSpec = tween(300), label = "扫雷教程步骤") { step ->
                Text(when (step) {
                    0 -> "点击格子翻开它。第一下不会踩雷。"
                    1 -> "这里的 1 说明周围八格恰好有一颗雷。"
                    2 -> "长按可插旗；也可以先开启插旗模式。"
                    else -> "翻开所有安全格就获胜，踩中地雷则失败。"
                }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun MinesweeperGameRoute(
    game: MinesweeperGame,
    onReveal: (Int) -> Unit,
    onFlag: (Int) -> Unit,
    onChord: (Int) -> Unit,
    onRestart: () -> Unit,
    onTick: () -> Unit,
    onBack: () -> Unit,
) {
    var flagMode by remember { mutableStateOf(false) }
    LaunchedEffect(game.started, game.status) {
        while (game.started && game.status == MinesweeperStatus.Playing) {
            delay(1_000)
            onTick()
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val wide = maxWidth >= 900.dp
        val cellSize = minOf(40.dp, (maxWidth - 48.dp) / game.difficulty.columns)
            .coerceAtLeast(26.dp)
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 1120.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回扫雷小岛")
                    }
                    Text("${game.difficulty.label}扫雷", Modifier.weight(1f).padding(start = 12.dp),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(formatMinesweeperTime(game.elapsedSeconds),
                        style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MinesweeperMetric("剩余地雷", game.minesRemaining, Modifier.weight(1f))
                    MinesweeperMetric("已翻开", game.revealed.size, Modifier.weight(1f))
                }
                AnimatedVisibility(game.status != MinesweeperStatus.Playing) {
                    Card(Modifier.fillMaxWidth().padding(top = 12.dp),
                        colors = CardDefaults.cardColors(containerColor =
                            if (game.status == MinesweeperStatus.Won) Color(0xFFE3EFD7) else mineColor)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(if (game.status == MinesweeperStatus.Won) "成功排雷！" else "踩到地雷了",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = onRestart) { Text("再来一局") }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                val board: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        MinesweeperBoard(game, cellSize, flagMode, onReveal, onFlag, onChord)
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(selected = flagMode, onClick = { flagMode = !flagMode },
                            label = { Text(if (flagMode) "⚑ 插旗模式已开启" else "⚑ 开启插旗模式") })
                        OutlinedButton(onClick = onRestart) { Text("重新开局") }
                        Text("点按翻格，长按插旗。数字格周围的旗子足够时，点按数字可展开邻格。",
                            style = MaterialTheme.typography.bodySmall)
                        if (game.difficulty == MinesweeperDifficulty.Hard && !wide) {
                            Text("可左右滑动棋盘查看完整区域。",
                                style = MaterialTheme.typography.bodySmall, color = IslandGreen)
                        }
                    }
                }
                if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(Modifier.weight(1f)) { board() }
                    Box(Modifier.width(250.dp)) { controls() }
                } else {
                    board()
                    Spacer(Modifier.height(14.dp))
                    controls()
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MinesweeperMetric(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.toString(), style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MinesweeperBoard(
    game: MinesweeperGame,
    cellSize: Dp,
    flagMode: Boolean,
    onReveal: (Int) -> Unit,
    onFlag: (Int) -> Unit,
    onChord: (Int) -> Unit,
) {
    Column(Modifier.width(cellSize * game.difficulty.columns)
        .background(Color(0xFFBED5BB), RoundedCornerShape(16.dp)).padding(4.dp)) {
        repeat(game.difficulty.rows) { row ->
            Row {
                repeat(game.difficulty.columns) { column ->
                    val index = row * game.difficulty.columns + column
                    val opened = index in game.revealed
                    val flagged = index in game.flags
                    val mine = index in game.mines
                    val lost = game.status == MinesweeperStatus.Lost
                    val number = if (opened) game.adjacentMines(index) else 0
                    val color = when {
                        index == game.exploded -> mineColor
                        lost && flagged && !mine -> mineColor
                        flagged -> flagColor
                        opened -> openedColor
                        lost && mine -> Color(0xFFFFE7DD)
                        else -> hiddenColor
                    }
                    Box(Modifier.size(cellSize).padding(1.dp).clip(RoundedCornerShape(5.dp))
                        .background(color).combinedClickable(
                            enabled = game.status == MinesweeperStatus.Playing,
                            onClick = {
                                when {
                                    opened -> onChord(index)
                                    flagMode -> onFlag(index)
                                    else -> onReveal(index)
                                }
                            },
                            onLongClick = { onFlag(index) },
                        ), contentAlignment = Alignment.Center) {
                        val label = when {
                            lost && flagged && !mine -> "×"
                            flagged -> "⚑"
                            lost && mine -> "✹"
                            opened && number > 0 -> number.toString()
                            else -> ""
                        }
                        Text(label, fontSize = if (cellSize < 30.dp) 14.sp else 18.sp,
                            fontWeight = FontWeight.Black, color = when {
                                lost && mine || lost && flagged && !mine -> Color(0xFFC44D45)
                                flagged -> Ink
                                number == 1 -> Color(0xFF4679B2)
                                number == 2 -> IslandGreen
                                number == 3 -> Color(0xFFC46B59)
                                else -> Ink
                            })
                    }
                }
            }
        }
    }
}

internal fun formatMinesweeperTime(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
