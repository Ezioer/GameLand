package com.zxq.sudoku.games.game2048

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.delay
import kotlin.math.abs

private val BoardBackground = Color(0xFFE8CC9D)

@Composable
internal fun Game2048IslandRoute(statistics: Game2048Statistics, canContinue: Boolean,
    onBack: () -> Unit, onStart: () -> Unit, onContinue: () -> Unit) {
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
                    Text("2048 小岛", style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold)
                    Text("轻轻一滑，数字长大。", color = IslandGreen)
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("玩法教程", Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge, color = IslandGreen)
                        IconButton(onClick = { replay++ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "重播 2048 教程")
                        }
                    }
                    Text("相同数字合并，挑战 2048", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Game2048Tutorial(replay)
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onContinue, enabled = canContinue,
                modifier = Modifier.fillMaxWidth()) { Text("继续") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("开一局") }
            Spacer(Modifier.height(16.dp))
            Game2048RecordCard(statistics)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Game2048Tutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        step = 0
        repeat(3) { delay(1_500); step = it + 1 }
    }
    val boards = remember {
        listOf(
            listOf(2, 0, 2, 0) + List(12) { 0 },
            listOf(4, 0, 0, 0) + List(12) { 0 },
            listOf(4, 4, 0, 0) + List(12) { 0 },
            listOf(8, 0, 0, 0) + List(12) { 0 },
        )
    }
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 260.dp).fillMaxWidth()) { Game2048Board(boards[step]) }
            Spacer(Modifier.height(10.dp))
            Crossfade(step, animationSpec = tween(300), label = "2048 教程步骤") { phase ->
                Text(when (phase) {
                    0 -> "向任意方向滑动，所有方块会一起移动。"
                    1 -> "两个相同的数字碰到一起，就会合并并得分。"
                    2 -> "每次有效移动后，棋盘会新增一个数字。"
                    else -> "合出 2048 即可达成目标，也可以继续挑战高分。"
                }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun Game2048RecordCard(statistics: Game2048Statistics) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("目前的记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("最高分 ${statistics.bestScore}", style = MaterialTheme.typography.headlineMedium,
                color = IslandGreen, fontWeight = FontWeight.Black)
            Text("最大方块 ${statistics.bestTile} · 已开 ${statistics.played} 局 · 达成 2048 ${statistics.wins} 次",
                style = MaterialTheme.typography.bodyMedium)
            if (statistics.bestAt.isNotEmpty())
                Text("刷新于 ${statistics.bestAt}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun Game2048GameRoute(game: Game2048Game, statistics: Game2048Statistics,
    onGameChange: (Game2048Game) -> Unit, onRestart: () -> Unit, onBack: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val slide: (SlideDirection) -> Unit = { direction -> onGameChange(game.slide(direction)) }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) false else {
                val direction = when (event.key) {
                    Key.DirectionUp -> SlideDirection.Up
                    Key.DirectionDown -> SlideDirection.Down
                    Key.DirectionLeft -> SlideDirection.Left
                    Key.DirectionRight -> SlideDirection.Right
                    else -> null
                }
                if (direction == null) false else { slide(direction); true }
            }
        }.focusRequester(focusRequester).focusable()) {
        val wide = maxWidth >= 820.dp
        val boardWidth = minOf(500.dp, maxWidth - 344.dp)
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = if (wide) 1000.dp else 620.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回 2048 小岛")
                    }
                    Text("2048", Modifier.weight(1f).padding(start = 12.dp),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("目标 2048", color = IslandGreen, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Game2048ScoreCard("本局", game.score, Modifier.weight(1f))
                    Game2048ScoreCard("最高", statistics.bestScore, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                val boardContent: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth().pointerInput(slide) {
                        var drag = Offset.Zero
                        detectDragGestures(
                            onDragStart = { drag = Offset.Zero },
                            onDragEnd = {
                                if (abs(drag.x) > 24f || abs(drag.y) > 24f) {
                                    slide(if (abs(drag.x) > abs(drag.y)) {
                                        if (drag.x > 0) SlideDirection.Right else SlideDirection.Left
                                    } else {
                                        if (drag.y > 0) SlideDirection.Down else SlideDirection.Up
                                    })
                                }
                            },
                            onDrag = { change, amount -> change.consume(); drag += amount },
                        )
                    }) {
                        Game2048Board(game.board)
                        if (game.won || game.finished) {
                            Game2048ResultOverlay(game, { onGameChange(game.continuePlaying()) },
                                onRestart, Modifier.align(Alignment.Center))
                        }
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("已走 ${game.moves} 步 · 最大方块 ${game.maxTile}",
                            style = MaterialTheme.typography.bodyMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { onGameChange(game.undo()) },
                                enabled = game.history.isNotEmpty(), modifier = Modifier.weight(1f)) {
                                Text("悔棋")
                            }
                            OutlinedButton(onClick = onRestart, modifier = Modifier.weight(1f)) {
                                Text("重开")
                            }
                        }
                        Text("滑动棋盘，或使用方向键。", style = MaterialTheme.typography.bodySmall)
                        Game2048DirectionPad(slide)
                    }
                }
                if (wide) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(boardWidth)) { boardContent() }
                        Spacer(Modifier.width(24.dp))
                        Box(Modifier.width(280.dp)) { controls() }
                    }
                } else {
                    boardContent()
                    Spacer(Modifier.height(16.dp))
                    controls()
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Game2048ScoreCard(label: String, score: Int, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(score.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun Game2048DirectionPad(onSlide: (SlideDirection) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(onClick = { onSlide(SlideDirection.Up) }) { Text("↑") }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { onSlide(SlideDirection.Left) }) { Text("←") }
            OutlinedButton(onClick = { onSlide(SlideDirection.Down) }) { Text("↓") }
            OutlinedButton(onClick = { onSlide(SlideDirection.Right) }) { Text("→") }
        }
    }
}

@Composable
private fun Game2048ResultOverlay(game: Game2048Game, onContinue: () -> Unit,
    onRestart: () -> Unit, modifier: Modifier = Modifier) {
    val scale = remember(game.won, game.finished) { Animatable(0.2f) }
    LaunchedEffect(game.won, game.finished) {
        scale.animateTo(1.12f, tween(380, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    Surface(modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(24.dp), color = Color(0xFFFFF9E9), shadowElevation = 12.dp) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (game.won) "达成 2048！" else "本局结束", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black, color = Ink)
            Text("得分 ${game.score}", style = MaterialTheme.typography.bodyLarge)
            if (game.won && !game.finished) Button(onClick = onContinue) { Text("继续挑战") }
            OutlinedButton(onClick = onRestart) { Text("再来一局") }
        }
    }
}

@Composable
internal fun Game2048Board(board: List<Int>) {
    Column(Modifier.fillMaxWidth().aspectRatio(1f)
        .background(BoardBackground, RoundedCornerShape(22.dp)).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(Game2048Size) { row ->
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(Game2048Size) { column ->
                    val value = board[row * Game2048Size + column]
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Crossfade(value, animationSpec = tween(180), label = "数字方块") { tile ->
                            Surface(Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp),
                                color = tileColor(tile)) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (tile != 0) Text(tile.toString(),
                                        color = if (tile < 8) Ink else Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = when { tile >= 1024 -> 22.sp; tile >= 128 -> 27.sp; else -> 32.sp },
                                        maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun tileColor(value: Int): Color = when (value) {
    0 -> Color(0x66FFF9EA)
    2 -> Color(0xFFFFF9EA)
    4 -> Color(0xFFFFEDC8)
    8 -> Color(0xFFF2B780)
    16 -> Color(0xFFEDA66D)
    32 -> Color(0xFFE88965)
    64 -> Color(0xFFDB715A)
    128 -> Color(0xFFC3B678)
    256 -> Color(0xFFB2AF67)
    512 -> Color(0xFF97A76B)
    1024 -> Color(0xFF78A38D)
    else -> IslandGreen
}
