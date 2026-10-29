package com.zxq.sudoku.games.klotski

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

private val BoardColor = Color(0xFFD9BE98)
private val CellColor = Color(0xFFFFF2DC)
private val CaoColor = Color(0xFFD97462)
private val GeneralColor = Color(0xFF85A994)
private val GuanColor = Color(0xFFDEAF71)
private val SoldierColor = Color(0xFFC7D6B4)

@Composable
internal fun KlotskiIslandRoute(
    statistics: KlotskiStatistics, savedGame: KlotskiGame?,
    onBack: () -> Unit, onContinue: () -> Unit, onStartLevel: (Int) -> Unit
) {
    var replay by remember { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val columns = if (maxWidth >= 720.dp) 4 else 2
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 900.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回游戏合集")
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(
                            "华容道小岛", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("为曹操让出一条路。", color = IslandGreen)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "玩法教程", Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge, color = IslandGreen
                            )
                            IconButton(onClick = { replay++ }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "重播华容道教程")
                            }
                        }
                        Text(
                            "移动棋子，送曹操走到出口", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        KlotskiTutorial(replay)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onContinue, enabled = savedGame?.completed == false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (savedGame == null) "继续" else "继续 · ${KlotskiLevels[savedGame.levelIndex].title}")
                }
                Spacer(Modifier.height(20.dp))
                Text("选择关卡", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                KlotskiLevels.indices.chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { index ->
                            val level = KlotskiLevels[index]
                            val record = statistics.forLevel(index)
                            Card(
                                Modifier.weight(1f).clickable { onStartLevel(index) },
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        if (index % 2 == 0) Color(0xFFF5E3D6) else Color(0xFFE3EFD7),
                                    contentColor = Ink,
                                )
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        level.difficulty, style = MaterialTheme.typography.labelLarge,
                                        color = IslandGreen
                                    )
                                    Text(
                                        level.title, style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        if (record.bestMoves == null) "尚未通关"
                                        else "最佳 ${record.bestMoves} 步",
                                        style = MaterialTheme.typography.bodySmall
                                    )
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
private fun KlotskiTutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        step = 0
        delay(1_700)
        step = 1
        delay(1_700)
        step = 2
    }
    val start = remember { KlotskiGame.start(0) }
    val game = if (step == 0) start else start.move(0, KlotskiDirection.Right)
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 176.dp).fillMaxWidth()) {
                KlotskiBoard(game, selectedPiece = 0, onSelect = {}, interactive = false)
            }
            Spacer(Modifier.height(8.dp))
            Crossfade(step, animationSpec = tween(300), label = "华容道教程步骤") { phase ->
                Text(
                    when (phase) {
                        0 -> "拖动棋子，或点选后用方向按钮挪动。"
                        1 -> "棋子不能重叠，也不能走出棋盘。"
                        else -> "让曹操到达底部中央出口即可过关。"
                    }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun KlotskiGameRoute(
    game: KlotskiGame, onMove: (Int, KlotskiDirection, Int) -> Unit,
    onUndo: () -> Unit, onRestart: () -> Unit, onNext: () -> Unit,
    onTick: () -> Unit, onBack: () -> Unit
) {
    var selectedPiece by rememberSaveable(game.levelIndex) { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(game.levelIndex, game.completed) {
        while (!game.completed) {
            delay(1_000); onTick()
        }
    }
    BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) false else {
                val direction = when (event.key) {
                    Key.DirectionUp -> KlotskiDirection.Up
                    Key.DirectionDown -> KlotskiDirection.Down
                    Key.DirectionLeft -> KlotskiDirection.Left
                    Key.DirectionRight -> KlotskiDirection.Right
                    else -> null
                }
                if (direction == null) false else {
                    onMove(selectedPiece, direction, 1); true
                }
            }
        }.focusRequester(focusRequester).focusable()
    ) {
        val wide = maxWidth >= 820.dp
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = if (wide) 900.dp else 620.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回华容道小岛")
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            KlotskiLevels[game.levelIndex].title,
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                        )
                        Text(
                            "送曹操走到下方出口", style = MaterialTheme.typography.bodySmall,
                            color = IslandGreen
                        )
                    }
                    Text(formatKlotskiTime(game.elapsedSeconds), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(18.dp))
                val board: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth()) {
                        KlotskiBoard(
                            game, selectedPiece,
                            onSelect = { selectedPiece = it }, onSlide = onMove
                        )
                        if (game.completed) KlotskiResultOverlay(
                            game, onNext, onRestart,
                            Modifier.align(Alignment.Center)
                        )
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                Text(
                                    "已走 ${game.moves} 步", style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "当前棋子：${KlotskiPieces[selectedPiece].name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onUndo, enabled = game.history.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text("撤回") }
                            OutlinedButton(onClick = onRestart, modifier = Modifier.weight(1f)) {
                                Text("重开")
                            }
                        }
                        Text("拖动棋子，或点选棋子后按方向键。", style = MaterialTheme.typography.bodySmall)
                        KlotskiDirectionPad { direction -> onMove(selectedPiece, direction, 1) }
                    }
                }
                if (wide) {
                    Row(
                        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(390.dp)) { board() }
                        Spacer(Modifier.width(24.dp))
                        Box(Modifier.width(300.dp)) { controls() }
                    }
                } else {
                    Box(Modifier.widthIn(max = 390.dp).fillMaxWidth()) { board() }
                    Spacer(Modifier.height(16.dp))
                    controls()
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun KlotskiDirectionPad(onMove: (KlotskiDirection) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(onClick = { onMove(KlotskiDirection.Up) }) { Text("↑") }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { onMove(KlotskiDirection.Left) }) { Text("←") }
            OutlinedButton(onClick = { onMove(KlotskiDirection.Down) }) { Text("↓") }
            OutlinedButton(onClick = { onMove(KlotskiDirection.Right) }) { Text("→") }
        }
    }
}

@Composable
private fun KlotskiResultOverlay(
    game: KlotskiGame, onNext: () -> Unit,
    onRestart: () -> Unit, modifier: Modifier = Modifier
) {
    val scale = remember(game.levelIndex) { Animatable(0.2f) }
    LaunchedEffect(game.levelIndex) {
        scale.animateTo(1.1f, tween(350, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    Surface(
        modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(24.dp), color = Color(0xFFFFF9E9), shadowElevation = 12.dp
    ) {
        Column(
            Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "曹操脱险！", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black, color = Ink
            )
            Text("${game.moves} 步 · ${formatKlotskiTime(game.elapsedSeconds)}")
            if (game.levelIndex < KlotskiLevels.lastIndex)
                Button(onClick = onNext) { Text("下一关") }
            OutlinedButton(onClick = onRestart) { Text("再玩一次") }
        }
    }
}

@Composable
internal fun KlotskiBoard(
    game: KlotskiGame, selectedPiece: Int?, onSelect: (Int) -> Unit,
    onSlide: (Int, KlotskiDirection, Int) -> Unit = { _, _, _ -> }, interactive: Boolean = true
) {
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnSlide by rememberUpdatedState(onSlide)
    Column(Modifier.fillMaxWidth()) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().aspectRatio(4f / 5f)
                .background(BoardColor, RoundedCornerShape(20.dp))
        ) {
            val cell = maxWidth / KlotskiColumns
            val cellPx = with(LocalDensity.current) { cell.toPx() }
            Column(Modifier.fillMaxSize()) {
                repeat(KlotskiRows) { row ->
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        repeat(KlotskiColumns) { column ->
                            Box(
                                Modifier.weight(1f).fillMaxHeight().padding(3.dp)
                                    .background(
                                        if (row == 4 && column in 1..2) Color(0xFFFFDFC0)
                                        else CellColor, RoundedCornerShape(12.dp)
                                    )
                            )
                        }
                    }
                }
            }
            KlotskiPieces.forEachIndexed { index, piece ->
                val topLeft = game.positions[index]
                val x by animateDpAsState(cell * (topLeft % KlotskiColumns), tween(210), label = "棋子横向移动")
                val y by animateDpAsState(cell * (topLeft / KlotskiColumns), tween(210), label = "棋子纵向移动")
                val shape = RoundedCornerShape(12.dp)
                val color = when (index) {
                    0 -> CaoColor
                    in 1..4 -> GeneralColor
                    5 -> GuanColor
                    else -> SoldierColor
                }
                val gesture = if (interactive) Modifier.clickable { currentOnSelect(index) }
                    .pointerInput(index, cellPx) {
                        var drag = Offset.Zero
                        detectDragGestures(
                            onDragStart = { drag = Offset.Zero; currentOnSelect(index) },
                            onDragEnd = {
                                val horizontal = abs(drag.x) > abs(drag.y)
                                val distance = if (horizontal) abs(drag.x) else abs(drag.y)
                                if (distance > 20f) {
                                    val direction = if (horizontal) {
                                        if (drag.x > 0) KlotskiDirection.Right else KlotskiDirection.Left
                                    } else {
                                        if (drag.y > 0) KlotskiDirection.Down else KlotskiDirection.Up
                                    }
                                    currentOnSlide(index, direction, maxOf(1, (distance / cellPx).roundToInt()))
                                }
                            },
                            onDrag = { change, amount -> change.consume(); drag += amount },
                        )
                    } else Modifier
                Box(
                    Modifier.offset(x, y).size(cell * piece.width, cell * piece.height)
                        .padding(3.dp).background(color, shape)
                        .then(if (selectedPiece == index) Modifier.border(3.dp, Color.White, shape) else Modifier)
                        .then(gesture), contentAlignment = Alignment.Center
                ) {
                    Text(
                        piece.name, color = if (index == 0) Color.White else Ink,
                        style = if (index == 0) MaterialTheme.typography.titleLarge
                        else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                        fontSize = if (index >= 6) 20.sp else if (index == 0) 28.sp else 18.sp,
                        maxLines = 1
                    )
                }
            }
        }
        Text(
            "↓ 出口", Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge, color = IslandGreen
        )
    }
}

internal fun formatKlotskiTime(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
