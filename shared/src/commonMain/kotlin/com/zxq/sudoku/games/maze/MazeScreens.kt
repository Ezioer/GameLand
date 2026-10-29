package com.zxq.sudoku.games.maze

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.delay
import kotlin.math.abs

private val floorColor = Color(0xFFFFFAF0)
private val trailColor = Color(0xFFD8E8DA)
private val exitColor = Color(0xFFF3C771)
private val playerColor = Color(0xFFDB826F)

@Composable
internal fun MazeIslandRoute(
    statistics: MazeStatistics,
    savedGame: MazeGame?,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onStart: (MazeDifficulty) -> Unit,
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
                    Text("迷宫小岛", style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold)
                    Text("选一条路，慢慢走出去。", color = IslandGreen)
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
                            Icon(Icons.Filled.Refresh, contentDescription = "重播迷宫教程")
                        }
                    }
                    Text("绕开墙壁，找到出口", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    MazeTutorial(replay)
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onContinue, enabled = savedGame?.completed == false,
                modifier = Modifier.fillMaxWidth()) {
                Text(if (savedGame == null) "继续" else "继续 · ${savedGame.difficulty.label}")
            }
            Spacer(Modifier.height(20.dp))
            Text("选择难度", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            MazeDifficulty.entries.forEachIndexed { index, difficulty ->
                val record = statistics.forDifficulty(difficulty)
                Card(Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onStart(difficulty) },
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
                            Text("${difficulty.size} × ${difficulty.size} · ${if (record.bestMoves == null) "尚未通关" else "最佳 ${record.bestMoves} 步"}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${record.completed}/${record.played}",
                            style = MaterialTheme.typography.labelLarge, color = IslandGreen)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MazeTutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    val start = remember { MazeGame.start(MazeDifficulty.Easy) { 0 } }
    val path = remember(start) { start.shortestPath() }
    LaunchedEffect(replay) {
        step = 0
        repeat(6) { delay(850); step = it + 1 }
    }
    val game = start.copy(player = path.getOrNull(step - 1) ?: start.start,
        visited = setOf(start.start) + path.take(step))
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 220.dp).fillMaxWidth()) { MazeBoard(game) }
            Spacer(Modifier.height(10.dp))
            Crossfade(step, animationSpec = tween(300), label = "迷宫教程步骤") { phase ->
                Text(if (phase < 6) "沿着浅色通道移动，绿色墙壁不能穿过。"
                    else "走到金色出口即可通关；迷路时可以点提示。",
                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun MazeGameRoute(
    game: MazeGame,
    onMove: (MazeDirection) -> Unit,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit,
    onNext: () -> Unit,
    onTick: () -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(game.difficulty, game.completed) {
        while (!game.completed) { delay(1_000); onTick() }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) false else {
                val direction = when (event.key) {
                    Key.DirectionUp -> MazeDirection.Up
                    Key.DirectionDown -> MazeDirection.Down
                    Key.DirectionLeft -> MazeDirection.Left
                    Key.DirectionRight -> MazeDirection.Right
                    else -> null
                }
                if (direction == null) false else { onMove(direction); true }
            }
        }.focusRequester(focusRequester).focusable()) {
        val wide = maxWidth >= 900.dp
        val boardWidth = minOf(500.dp, maxWidth - 360.dp)
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = if (wide) 1000.dp else 620.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回迷宫小岛")
                    }
                    Text("${game.difficulty.label}迷宫", Modifier.weight(1f).padding(start = 12.dp),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(formatMazeTime(game.elapsedSeconds), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MazeMetric("步数", game.moves, Modifier.weight(1f))
                    MazeMetric("提示", game.hintsUsed, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                val board: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth().pointerInput(game.player) {
                        var drag = Offset.Zero
                        detectDragGestures(onDragStart = { drag = Offset.Zero },
                            onDragEnd = {
                                if (abs(drag.x) > 24f || abs(drag.y) > 24f) {
                                    onMove(if (abs(drag.x) > abs(drag.y)) {
                                        if (drag.x > 0) MazeDirection.Right else MazeDirection.Left
                                    } else {
                                        if (drag.y > 0) MazeDirection.Down else MazeDirection.Up
                                    })
                                }
                            }, onDrag = { change, amount -> change.consume(); drag += amount })
                    }) {
                        MazeBoard(game)
                        if (game.completed) MazeResultOverlay(game, onNext, onRestart,
                            Modifier.align(Alignment.Center))
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onUndo, enabled = game.history.isNotEmpty(),
                                modifier = Modifier.weight(1f)) { Text("撤回") }
                            OutlinedButton(onClick = onHint, enabled = !game.completed,
                                modifier = Modifier.weight(1f)) { Text("提示") }
                            OutlinedButton(onClick = onRestart,
                                modifier = Modifier.weight(1f)) { Text("重开") }
                        }
                        Text(if (game.hintCell == null) "滑动棋盘或用方向键移动。金色圆点是出口。"
                            else "浅绿色圆点是建议的下一步。",
                            style = MaterialTheme.typography.bodySmall)
                        MazeDirectionPad(onMove)
                    }
                }
                if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(boardWidth)) { board() }
                    Spacer(Modifier.width(24.dp))
                    Box(Modifier.width(280.dp)) { controls() }
                } else {
                    board()
                    Spacer(Modifier.height(16.dp))
                    controls()
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MazeMetric(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.toString(), style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun MazeDirectionPad(onMove: (MazeDirection) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(onClick = { onMove(MazeDirection.Up) }) { Text("↑") }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { onMove(MazeDirection.Left) }) { Text("←") }
            OutlinedButton(onClick = { onMove(MazeDirection.Down) }) { Text("↓") }
            OutlinedButton(onClick = { onMove(MazeDirection.Right) }) { Text("→") }
        }
    }
}

@Composable
private fun MazeResultOverlay(game: MazeGame, onNext: () -> Unit,
    onRestart: () -> Unit, modifier: Modifier = Modifier) {
    val scale = remember(game.difficulty) { Animatable(0.2f) }
    LaunchedEffect(game.difficulty) {
        scale.animateTo(1.1f, tween(350, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    Surface(modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(24.dp), color = Color(0xFFFFF9E9), shadowElevation = 12.dp) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("走出迷宫啦！", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black, color = Ink)
            Text("${game.moves} 步 · ${formatMazeTime(game.elapsedSeconds)} · ${game.hintsUsed} 次提示")
            Button(onClick = onNext) {
                Text(if (game.difficulty == MazeDifficulty.Hard) "再来一局" else "下一难度")
            }
            OutlinedButton(onClick = onRestart) { Text("重玩本图") }
        }
    }
}

@Composable
internal fun MazeBoard(game: MazeGame) {
    val animatedX by animateFloatAsState(game.player.rem(game.size).toFloat(),
        animationSpec = tween(230, easing = FastOutSlowInEasing), label = "迷宫玩家横向移动")
    val animatedY by animateFloatAsState((game.player / game.size).toFloat(),
        animationSpec = tween(230, easing = FastOutSlowInEasing), label = "迷宫玩家纵向移动")
    Canvas(Modifier.fillMaxWidth().aspectRatio(1f)
        .background(Color(0xFFDCEAD9), RoundedCornerShape(22.dp)).padding(7.dp)) {
        val unit = size.width / game.size
        repeat(game.size * game.size) { index ->
            val x = index % game.size
            val y = index / game.size
            val color = when {
                index in game.walls -> IslandGreen
                index in game.visited -> trailColor
                else -> floorColor
            }
            drawRoundRect(color,
                topLeft = Offset(x * unit + 0.6f, y * unit + 0.6f),
                size = Size(unit - 1.2f, unit - 1.2f),
                cornerRadius = CornerRadius(unit * 0.22f))
        }
        fun marker(index: Int, color: Color, radius: Float) {
            drawCircle(color, radius = unit * radius,
                center = Offset((index % game.size + 0.5f) * unit,
                    (index / game.size + 0.5f) * unit))
        }
        marker(game.start, Color(0xFF9DC6AA), 0.22f)
        marker(game.exit, exitColor, 0.38f)
        marker(game.exit, floorColor, 0.14f)
        game.hintCell?.let { marker(it, Color(0xFF8ECB89), 0.28f) }
        drawCircle(playerColor, radius = unit * 0.36f,
            center = Offset((animatedX + 0.5f) * unit, (animatedY + 0.5f) * unit))
        drawCircle(Color.White.copy(alpha = 0.68f), radius = unit * 0.1f,
            center = Offset((animatedX + 0.42f) * unit, (animatedY + 0.4f) * unit))
    }
}

internal fun formatMazeTime(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
