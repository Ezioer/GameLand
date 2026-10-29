package com.zxq.sudoku.games.sokoban

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
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

private val FloorColor = Color(0xFFFFF8E8)
private val WallColor = Color(0xFF8FAD9B)
private val CrateColor = Color(0xFFD7A568)
private val GoalColor = Color(0xFFFFD79C)

@Composable
internal fun SokobanIslandRoute(statistics: SokobanStatistics, savedGame: SokobanGame?,
    onBack: () -> Unit, onContinue: () -> Unit, onStartLevel: (Int) -> Unit) {
    var replay by remember { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val columns = if (maxWidth >= 720.dp) 3 else 2
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 900.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回游戏合集")
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("推箱子小岛", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold)
                        Text("慢慢走，想好再推。", color = IslandGreen)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("玩法教程", Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge, color = IslandGreen)
                            IconButton(onClick = { replay++ }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "重播推箱子教程")
                            }
                        }
                        Text("把所有箱子推到目标点", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        SokobanTutorial(replay)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = onContinue, enabled = savedGame?.completed == false,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (savedGame == null) "继续" else "继续 · 第 ${savedGame.levelIndex + 1} 关")
                }
                Spacer(Modifier.height(20.dp))
                Text("选择关卡", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                SokobanLevels.indices.chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { index ->
                            val record = statistics.forLevel(index)
                            Card(Modifier.weight(1f).clickable { onStartLevel(index) },
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor =
                                    if (index % 2 == 0) Color(0xFFE3EFD7) else Color(0xFFFFEBCF),
                                    contentColor = Ink)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("第 ${index + 1} 关", style = MaterialTheme.typography.labelLarge,
                                        color = IslandGreen)
                                    Text(SokobanLevels[index].title,
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(if (record.bestMoves == null) "尚未通关"
                                        else "最佳 ${record.bestMoves} 步 · ${record.bestPushes} 推",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun SokobanTutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        step = 0
        delay(1_700)
        step = 1
        delay(1_700)
        step = 2
    }
    val start = remember { SokobanGame.start(0) }
    val game = if (step == 0) start else start.move(SokobanDirection.Up)
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 260.dp).fillMaxWidth()) { SokobanBoard(game) }
            Spacer(Modifier.height(10.dp))
            Crossfade(step, animationSpec = tween(300), label = "推箱子教程步骤") { phase ->
                Text(when (phase) {
                    0 -> "人物可以走动，只能推箱子，不能拉箱子。"
                    1 -> "站在箱子后方，向目标点推动。"
                    else -> "所有箱子都到目标点，就过关啦！"
                }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun SokobanGameRoute(game: SokobanGame, onMove: (SokobanDirection) -> Unit,
    onUndo: () -> Unit, onRestart: () -> Unit, onNext: () -> Unit,
    onTick: () -> Unit, onBack: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(game.levelIndex, game.completed) {
        while (!game.completed) { delay(1_000); onTick() }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) false else {
                val direction = when (event.key) {
                    Key.DirectionUp -> SokobanDirection.Up
                    Key.DirectionDown -> SokobanDirection.Down
                    Key.DirectionLeft -> SokobanDirection.Left
                    Key.DirectionRight -> SokobanDirection.Right
                    else -> null
                }
                if (direction == null) false else { onMove(direction); true }
            }
        }.focusRequester(focusRequester).focusable()) {
        val wide = maxWidth >= 820.dp
        val boardWidth = minOf(520.dp, maxWidth - 344.dp)
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = if (wide) 1000.dp else 620.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回推箱子小岛")
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("第 ${game.levelIndex + 1} 关 · ${game.level.title}",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("把所有箱子推到目标点", style = MaterialTheme.typography.bodySmall,
                            color = IslandGreen)
                    }
                    Text(formatSokobanTime(game.elapsedSeconds), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SokobanMetric("步数", game.moves, Modifier.weight(1f))
                    SokobanMetric("推动", game.pushes, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                val board: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth().pointerInput(game.player, game.crates) {
                        var drag = Offset.Zero
                        detectDragGestures(
                            onDragStart = { drag = Offset.Zero },
                            onDragEnd = {
                                if (abs(drag.x) > 24f || abs(drag.y) > 24f) {
                                    onMove(if (abs(drag.x) > abs(drag.y)) {
                                        if (drag.x > 0) SokobanDirection.Right else SokobanDirection.Left
                                    } else {
                                        if (drag.y > 0) SokobanDirection.Down else SokobanDirection.Up
                                    })
                                }
                            },
                            onDrag = { change, amount -> change.consume(); drag += amount },
                        )
                    }) {
                        SokobanBoard(game)
                        if (game.completed) SokobanResultOverlay(game, onNext, onRestart,
                            Modifier.align(Alignment.Center))
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onUndo, enabled = game.history.isNotEmpty(),
                                modifier = Modifier.weight(1f)) { Text("悔棋") }
                            OutlinedButton(onClick = onRestart, modifier = Modifier.weight(1f)) {
                                Text("重开")
                            }
                        }
                        Text("滑动棋盘，或用方向键移动。", style = MaterialTheme.typography.bodySmall)
                        SokobanDirectionPad(onMove)
                    }
                }
                if (wide) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(boardWidth)) { board() }
                        Spacer(Modifier.width(24.dp))
                        Box(Modifier.width(280.dp)) { controls() }
                    }
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
private fun SokobanMetric(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SokobanDirectionPad(onMove: (SokobanDirection) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(onClick = { onMove(SokobanDirection.Up) }) { Text("↑") }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { onMove(SokobanDirection.Left) }) { Text("←") }
            OutlinedButton(onClick = { onMove(SokobanDirection.Down) }) { Text("↓") }
            OutlinedButton(onClick = { onMove(SokobanDirection.Right) }) { Text("→") }
        }
    }
}

@Composable
private fun SokobanResultOverlay(game: SokobanGame, onNext: () -> Unit,
    onRestart: () -> Unit, modifier: Modifier = Modifier) {
    val scale = remember(game.levelIndex) { Animatable(0.2f) }
    LaunchedEffect(game.levelIndex) {
        scale.animateTo(1.1f, tween(350, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    Surface(modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(24.dp), color = Color(0xFFFFF9E9), shadowElevation = 12.dp) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("过关啦！", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black, color = Ink)
            Text("${game.moves} 步 · ${game.pushes} 推 · ${formatSokobanTime(game.elapsedSeconds)}")
            if (game.levelIndex < SokobanLevels.lastIndex)
                Button(onClick = onNext) { Text("下一关") }
            OutlinedButton(onClick = onRestart) { Text("再玩一次") }
        }
    }
}

@Composable
internal fun SokobanBoard(game: SokobanGame) {
    val level = game.level
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(level.width.toFloat() / level.height)
        .background(Color(0xFFD3E2C7), RoundedCornerShape(22.dp)).padding(8.dp)) {
        val textSize = (maxWidth.value / level.width * 0.42f).coerceIn(14f, 28f).sp
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(level.height) { y ->
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(level.width) { x ->
                        val index = y * level.width + x
                        val wall = index in level.walls
                        val goal = index in level.goals
                        val crate = index in game.crates
                        val player = index == game.player
                        val tileColor by animateColorAsState(
                            targetValue = when {
                                wall -> WallColor
                                crate -> if (goal) IslandGreen else CrateColor
                                goal -> GoalColor
                                else -> FloorColor
                            }, animationSpec = tween(220), label = "推箱子格子颜色")
                        Box(Modifier.weight(1f).fillMaxHeight()
                            .background(tileColor, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center) {
                            Crossfade(when { player -> "人"; crate -> "箱"; goal -> "•"; else -> "" },
                                animationSpec = tween(180), label = "推箱子棋子") { symbol ->
                                Text(symbol, color = if (crate && goal) Color.White else Ink,
                                    fontSize = textSize, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun formatSokobanTime(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
