package com.zxq.sudoku.games.tetris

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.delay

private val boardBackground = Color(0xFFDBEBE2)
private val emptyCell = Color(0xFFFFFAF0)

@Composable
internal fun TetrisIslandRoute(
    statistics: TetrisStatistics,
    canContinue: Boolean,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onContinue: () -> Unit,
    onFriend: () -> Unit,
    friendAvailable: Boolean,
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
                    Text("俄罗斯方块小岛", style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold)
                    Text("转动方块，拼出整齐的一行。", color = IslandGreen)
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
                            Icon(Icons.Filled.Refresh, contentDescription = "重播俄罗斯方块教程")
                        }
                    }
                    Text("填满一整行，就能消除", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    TetrisTutorial(replay)
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onContinue, enabled = canContinue,
                modifier = Modifier.fillMaxWidth()) { Text("继续") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("开一局") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onFriend, enabled = friendAvailable,
                modifier = Modifier.fillMaxWidth()) { Text("好友对战 · 附近连接") }
            Spacer(Modifier.height(16.dp))
            TetrisRecordCard(statistics)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TetrisTutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        step = 0
        repeat(3) { delay(1_500); step = it + 1 }
    }
    val board = remember {
        MutableList<Tetromino?>(TetrisCells) { null }.apply {
            for (col in 0 until TetrisWidth) if (col !in 3..6)
                this[(TetrisHeight - 1) * TetrisWidth + col] = Tetromino.O
        }
    }
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 150.dp).fillMaxWidth()) {
                TetrisBoard(if (step == 3) List(TetrisCells) { null } else board,
                    active = when (step) {
                        0 -> FallingPiece(Tetromino.I, y = 10)
                        1 -> FallingPiece(Tetromino.I, y = 15)
                        2 -> FallingPiece(Tetromino.I, y = 18)
                        else -> null
                    }, ghost = null)
            }
            Spacer(Modifier.height(10.dp))
            Crossfade(step, animationSpec = tween(300), label = "俄罗斯方块教程步骤") { phase ->
                Text(when (phase) {
                    0 -> "左右移动方块，旋转后对准空位。"
                    1 -> "方块会自动下落，也能按住向下加速。"
                    2 -> "落下的方块填满整行。"
                    else -> "整行消除并得分；越往后，下落越快。"
                }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun TetrisRecordCard(statistics: TetrisStatistics) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("目前的记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("最高分 ${statistics.bestScore}", style = MaterialTheme.typography.headlineMedium,
                color = IslandGreen, fontWeight = FontWeight.Black)
            Text("最多消除 ${statistics.bestLines} 行 · 最高等级 ${statistics.bestLevel} · 已开 ${statistics.played} 局",
                style = MaterialTheme.typography.bodyMedium)
            if (statistics.bestAt.isNotEmpty()) Text("刷新于 ${statistics.bestAt}",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun TetrisGameRoute(
    game: TetrisGame,
    statistics: TetrisStatistics,
    onGameChange: (TetrisGame) -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    match: TetrisMatch? = null,
) {
    val latestGame by rememberUpdatedState(game)
    val focusRequester = remember { FocusRequester() }
    var confirmRestart by remember { mutableStateOf(false) }
    LaunchedEffect(match?.phase) {
        if (match?.phase == MatchPhase.Finished || match?.phase == MatchPhase.Disconnected) {
            if (game.playable) onGameChange(game.pause())
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(game.paused, game.finished, game.level) {
        while (!latestGame.paused && !latestGame.finished) {
            delay((850 - (latestGame.level - 1) * 65).coerceAtLeast(90).toLong())
            val current = latestGame
            if (current.playable) onGameChange(current.tick())
        }
    }
    LaunchedEffect(game.paused, game.finished) {
        while (!latestGame.paused && !latestGame.finished) {
            delay(1_000)
            val current = latestGame
            if (current.playable) onGameChange(current.second())
        }
    }
    val act: (TetrisGame.() -> TetrisGame) -> Unit = { action ->
        val current = latestGame
        if (current.playable) onGameChange(current.action())
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown || !latestGame.playable) false else {
                when (event.key) {
                    Key.DirectionLeft -> { act { move(-1) }; true }
                    Key.DirectionRight -> { act { move(1) }; true }
                    Key.DirectionUp -> { act { rotate() }; true }
                    Key.DirectionDown -> { act { softDrop() }; true }
                    else -> false
                }
            }
        }.focusRequester(focusRequester).focusable()) {
        val wide = maxWidth >= 960.dp
        val boardWidth = if (wide) minOf(350.dp, maxWidth - 400.dp)
            else minOf(270.dp, (maxWidth - 52.dp) * 0.68f)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = if (wide) 900.dp else 620.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回俄罗斯方块小岛")
                    }
                    Text("俄罗斯方块", Modifier.weight(1f).padding(start = 12.dp),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    FilledTonalButton(onClick = { onGameChange(if (game.paused) game.resume() else game.pause()) },
                        enabled = !game.finished && match == null) {
                        Icon(if (game.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (game.paused) "继续" else "暂停")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TetrisMetric("本局", game.score.toString(), Modifier.weight(1f))
                    TetrisMetric("最高", statistics.bestScore.toString(), Modifier.weight(1f))
                    TetrisMetric("等级", game.level.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                val boardAndNext: @Composable () -> Unit = {
                    Row(verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.width(boardWidth)) {
                            TetrisBoard(game.board, if (game.finished) null else game.active,
                                if (game.finished) null else game.ghost())
                            if (match == null && (game.finished || game.paused)) TetrisOverlay(game, onRestart,
                                onResume = { onGameChange(game.resume()) }, Modifier.align(Alignment.Center))
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("暂存", style = MaterialTheme.typography.labelSmall)
                            TetrisPreview(game.held)
                            Text("下一个", style = MaterialTheme.typography.labelSmall)
                            TetrisPreview(game.next.firstOrNull())
                            if (wide) {
                                Text("消除 ${game.lines} 行", style = MaterialTheme.typography.bodyMedium)
                                Text("用时 ${tetrisDuration(game.elapsedSeconds)}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            if (match != null) {
                                Text("好友进度", style = MaterialTheme.typography.labelMedium)
                                Box(Modifier.widthIn(max = 92.dp).fillMaxWidth()) {
                                    val friend = match.remoteGame
                                    TetrisBoard(friend?.board ?: List(TetrisCells) { null },
                                        friend?.active, null)
                                }
                                /*Text("好友已消除 ${match.remoteGame?.lines ?: 0} 行",
                                    style = MaterialTheme.typography.labelSmall)*/
                                if (match.remoteStale) Text("好友画面正在同步…",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!wide) Text("消除 ${game.lines} 行 · 用时 ${tetrisDuration(game.elapsedSeconds)}",
                            style = MaterialTheme.typography.bodyMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { act { move(-1) } }, enabled = game.playable,
                                modifier = Modifier.weight(1f)) { Text("←") }
                            OutlinedButton(onClick = { act { rotate() } }, enabled = game.playable,
                                modifier = Modifier.weight(1f)) { Text("旋转") }
                            OutlinedButton(onClick = { act { move(1) } }, enabled = game.playable,
                                modifier = Modifier.weight(1f)) { Text("→") }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { act { softDrop() } }, enabled = game.playable,
                                modifier = Modifier.weight(1f)) { Text("下移") }
                            Button(onClick = { act { hardDrop() } }, enabled = game.playable,
                                modifier = Modifier.weight(1f)) { Text("落下") }
                            OutlinedButton(onClick = { act { hold() } },
                                enabled = game.playable && game.canHold,
                                modifier = Modifier.weight(1f)) { Text("暂存") }
                        }
                        if (match == null) OutlinedButton(onClick = { confirmRestart = true },
                            modifier = Modifier.fillMaxWidth()) { Text("重开") }
                        Text("方向键移动与旋转；点击「落下」可立即落地。",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(560.dp)) { boardAndNext() }
                    Spacer(Modifier.width(24.dp))
                    Box(Modifier.width(280.dp)) { controls() }
                } else {
                    boardAndNext()
                    Spacer(Modifier.height(12.dp))
                    controls()
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    if (confirmRestart) AlertDialog(onDismissRequest = { confirmRestart = false },
        title = { Text("重开一局？") },
        text = { Text("当前进度会被新的对局替换。") },
        confirmButton = { TextButton(onClick = { confirmRestart = false; onRestart() }) { Text("重开") } },
        dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("继续当前局") } })
}

@Composable
private fun TetrisMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TetrisOverlay(game: TetrisGame, onRestart: () -> Unit,
    onResume: () -> Unit, modifier: Modifier = Modifier) {
    val scale = remember(game.finished) { Animatable(0.2f) }
    LaunchedEffect(game.finished) {
        scale.animateTo(1.1f, tween(350, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    Surface(modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(24.dp), color = Color(0xFFFFF9E9), shadowElevation = 12.dp) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (game.finished) "本局结束" else "已暂停",
                style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Black)
            Text("${game.score} 分 · 消除 ${game.lines} 行")
            if (game.finished) OutlinedButton(onClick = onRestart) { Text("再来一局") }
            else Button(onClick = onResume) { Text("继续") }
        }
    }
}

@Composable
internal fun TetrisBoard(board: List<Tetromino?>, active: FallingPiece?, ghost: FallingPiece?) {
    Canvas(Modifier.fillMaxWidth().aspectRatio(0.5f)
        .background(boardBackground, RoundedCornerShape(18.dp)).padding(5.dp)) {
        val unit = size.width / TetrisWidth
        fun tile(col: Int, row: Int, color: Color, inset: Float = 1.3f) {
            if (col !in 0 until TetrisWidth || row !in 0 until TetrisHeight) return
            drawRoundRect(color, topLeft = Offset(col * unit + inset, row * unit + inset),
                size = Size(unit - inset * 2, unit - inset * 2),
                cornerRadius = CornerRadius(unit * 0.17f))
        }
        repeat(TetrisHeight) { row -> repeat(TetrisWidth) { col ->
            tile(col, row, emptyCell.copy(alpha = 0.8f))
            board.getOrNull(row * TetrisWidth + col)?.let { tile(col, row, tetrominoColor(it)) }
        } }
        ghost?.cells?.forEach { (col, row) -> tile(col, row, tetrominoColor(ghost.kind).copy(alpha = 0.28f)) }
        active?.cells?.forEach { (col, row) -> tile(col, row, tetrominoColor(active.kind)) }
    }
}

@Composable
private fun TetrisPreview(kind: Tetromino?) {
    Canvas(Modifier.size(60.dp).background(Color(0xFFFFFAF1), RoundedCornerShape(14.dp)).padding(6.dp)) {
        val unit = size.width / 4
        kind?.let {
            FallingPiece(it, x = 0, y = 0).cells.forEach { (col, row) ->
                drawRoundRect(tetrominoColor(it), Offset(col * unit + 1f, row * unit + 1f),
                    Size(unit - 2f, unit - 2f), CornerRadius(unit * 0.17f))
            }
        }
    }
}

private fun tetrominoColor(kind: Tetromino): Color = when (kind) {
    Tetromino.I -> Color(0xFF5BB9C7)
    Tetromino.O -> Color(0xFFE9BA56)
    Tetromino.T -> Color(0xFFA986CA)
    Tetromino.S -> Color(0xFF83B981)
    Tetromino.Z -> Color(0xFFD9827D)
    Tetromino.J -> Color(0xFF6E96C7)
    Tetromino.L -> Color(0xFFE5A36F)
    Tetromino.G -> Color(0xFF89938F)
}

private fun tetrisDuration(seconds: Int): String =
    "${seconds / 60}分${(seconds % 60).toString().padStart(2, '0')}秒"
