package com.zxq.sudoku.games.gomoku

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val GomokuBoardColor = Color(0xFFF2D6A5)
private val BlackStoneColor = Color(0xFF354640)
private val WhiteStoneColor = Color(0xFFFFFAEF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GomokuIslandRoute(
    onBack: () -> Unit,
    onHuman: (GomokuLevel) -> Unit,
    onFriend: () -> Unit,
) {
    var chooseLevel by remember { mutableStateOf(false) }
    var replay by remember { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回游戏合集")
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text("五子棋小岛", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("落子成线，步步有趣。", color = IslandGreen)
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("玩法教程", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = IslandGreen)
                        IconButton(onClick = { replay++ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "重播五子棋教程")
                        }
                    }
                    Text("先把五颗棋子连成线", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    GomokuTutorial(replay)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("选择玩法", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            GomokuModeCard("人机对战", "简单、中级、困难，循序练习", "01", Color(0xFFFFEBC5)) {
                chooseLevel = true
            }
            Spacer(Modifier.height(12.dp))
            GomokuModeCard("好友对战", "同屏轮流落子，黑方先行", "02", Color(0xFFE6E5FA), onFriend)
            Spacer(Modifier.height(24.dp))
        }
    }
    if (chooseLevel) ModalBottomSheet(onDismissRequest = { chooseLevel = false }) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
            Text("选择难度", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            GomokuLevel.entries.forEach { level ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable {
                    chooseLevel = false
                    onHuman(level)
                }, shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        Text(level.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(when (level) {
                            GomokuLevel.Easy -> "适合初次了解五子棋"
                            GomokuLevel.Medium -> "会观察攻守与对手威胁"
                            GomokuLevel.Hard -> "会考虑落子后的应对"
                        }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun GomokuModeCard(title: String, subtitle: String, number: String, color: Color, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Ink)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.65f)) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(number, fontWeight = FontWeight.Bold, color = IslandGreen)
                }
            }
            Column(Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun GomokuTutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        step = 0
        repeat(5) { delay(900); step = it + 1 }
    }
    val board = remember(step) {
        MutableList<GomokuStone?>(GomokuCells) { null }.apply {
            repeat(step) { this[7 * GomokuSize + 5 + it] = GomokuStone.Black }
        }
    }
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 260.dp).fillMaxWidth()) {
                GomokuBoard(board, lastMove = if (step > 0) 7 * GomokuSize + 4 + step else null,
                    enabled = false, onPoint = {})
            }
            Spacer(Modifier.height(10.dp))
            Crossfade(step, animationSpec = tween(300), label = "五子棋教程步骤") { current ->
                Text(if (current == 5) "五颗同色棋子横、竖或斜连成一线即可获胜。"
                    else "黑方先行，双方轮流在交叉点落子。",
                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun GomokuGameRoute(
    game: GomokuGame,
    onGameChange: (GomokuGame) -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmResign by remember { mutableStateOf(false) }
    var offerDraw by remember { mutableStateOf(false) }
    var drawRejected by remember { mutableStateOf(false) }
    var showRules by remember { mutableStateOf(false) }
    val computerTurn = game.mode == GomokuMode.Human && game.turn == GomokuStone.White && !game.finished
    LaunchedEffect(game) {
        if (computerTurn) {
            delay(450)
            val index = withContext(Dispatchers.Default) {
                GomokuAi.choose(game.board, GomokuStone.White, game.level ?: GomokuLevel.Medium)
            }
            index?.let { game.place(it)?.let(onGameChange) }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val wide = maxWidth >= 820.dp
        val boardWidth = minOf(500.dp, maxWidth - 344.dp)
        Column(
            Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Column(Modifier.widthIn(max = if (wide) 1000.dp else 620.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回五子棋小岛")
                }
                Text(if (game.mode == GomokuMode.Friend) "好友对战" else "人机对战 · ${game.level?.label}",
                    Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                FilledTonalButton(onClick = { showRules = true }) { Text("规则") }
            }
            Spacer(Modifier.height(12.dp))
            Text(when {
                game.drawn -> "双方和棋"
                game.winner != null -> "${game.winner.label}获胜"
                computerTurn -> "白方思考中…"
                else -> "${game.turn.label}落子"
            }, style = MaterialTheme.typography.titleMedium, color = IslandGreen)
            Spacer(Modifier.height(10.dp))
            val boardContent: @Composable () -> Unit = {
                Box(Modifier.fillMaxWidth()) {
                    GomokuBoard(game.board, game.moves.lastOrNull(),
                        enabled = !game.finished && !computerTurn,
                        onPoint = { index -> game.place(index)?.let(onGameChange) })
                    if (game.finished) GomokuResultOverlay(game, Modifier.align(Alignment.Center))
                }
            }
            val controls: @Composable () -> Unit = {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("黑方已走 ${(game.moves.size + 1) / 2} 步", style = MaterialTheme.typography.labelLarge,
                            color = BlackStoneColor)
                        Text("白方已走 ${game.moves.size / 2} 步", style = MaterialTheme.typography.labelLarge,
                            color = IslandGreen)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onGameChange(game.undo()) },
                            enabled = !game.finished && !computerTurn && game.moves.isNotEmpty(),
                            modifier = Modifier.weight(1f)) { Text("悔棋") }
                        OutlinedButton(onClick = onRestart, modifier = Modifier.weight(1f)) { Text("重开") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { offerDraw = true },
                            enabled = !game.finished && !computerTurn, modifier = Modifier.weight(1f)) { Text("求和") }
                        Button(onClick = { confirmResign = true },
                            enabled = !game.finished && !computerTurn, modifier = Modifier.weight(1f)) { Text("认输") }
                    }
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
                controls()
            }
            Spacer(Modifier.height(20.dp))
        }
    }
    }
    if (confirmResign) AlertDialog(
        onDismissRequest = { confirmResign = false },
        title = { Text("确认认输？") },
        text = { Text("本局将由${game.turn.other().label}获胜。") },
        confirmButton = { TextButton(onClick = { confirmResign = false; onGameChange(game.resign()) }) { Text("认输") } },
        dismissButton = { TextButton(onClick = { confirmResign = false }) { Text("继续下棋") } },
    )
    if (offerDraw) AlertDialog(
        onDismissRequest = { offerDraw = false },
        title = { Text("请求和棋") },
        text = { Text(if (game.mode == GomokuMode.Friend)
            "${game.turn.label}提出和棋，${game.turn.other().label}是否同意？" else "向电脑提出和棋请求？") },
        confirmButton = { TextButton(onClick = {
            offerDraw = false
            if (game.mode == GomokuMode.Friend || GomokuAi.acceptsDraw(game)) onGameChange(game.agreeDraw())
            else drawRejected = true
        }) { Text(if (game.mode == GomokuMode.Friend) "同意和棋" else "提出请求") } },
        dismissButton = { TextButton(onClick = { offerDraw = false }) { Text("继续下棋") } },
    )
    if (drawRejected) AlertDialog(
        onDismissRequest = { drawRejected = false },
        title = { Text("求和未成功") },
        text = { Text("电脑拒绝了和棋请求，请继续下棋。") },
        confirmButton = { TextButton(onClick = { drawRejected = false }) { Text("知道了") } },
    )
    if (showRules) AlertDialog(
        onDismissRequest = { showRules = false },
        title = { Text("五子棋规则") },
        text = { Text("黑棋先行，双方轮流在棋盘交叉点落子。横、竖或斜向连续五颗同色棋子即可获胜；棋盘下满且无人获胜则为和局。") },
        confirmButton = { TextButton(onClick = { showRules = false }) { Text("知道了") } },
    )
}

@Composable
private fun GomokuResultOverlay(game: GomokuGame, modifier: Modifier = Modifier) {
    val result = when {
        game.drawn -> "和局"
        game.winner == GomokuStone.Black -> "黑方胜"
        else -> "白方胜"
    }
    val scale = remember(result) { Animatable(0.15f) }
    LaunchedEffect(result) {
        scale.animateTo(1.18f, tween(440, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Surface(modifier.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        alpha = ((scale.value - 0.15f) / 0.4f).coerceIn(0f, 1f)
    }, shape = RoundedCornerShape(24.dp), color = Color(0xFFFFF9E9),
        border = BorderStroke(2.dp, IslandGreen), shadowElevation = 12.dp) {
        Text(result, Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Ink)
    }
}

@Composable
internal fun GomokuBoard(
    board: List<GomokuStone?>,
    lastMove: Int?,
    enabled: Boolean,
    onPoint: (Int) -> Unit,
) {
    val stoneScale = remember(lastMove) { Animatable(if (lastMove == null) 1f else 0.5f) }
    LaunchedEffect(lastMove) {
        if (lastMove != null) {
            stoneScale.animateTo(1.1f, tween(220, easing = FastOutSlowInEasing))
            stoneScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)
        .background(GomokuBoardColor, RoundedCornerShape(16.dp))) {
        val cell = maxWidth / GomokuSize
        Canvas(Modifier.fillMaxSize()) {
            val unit = size.width / GomokuSize
            val ink = Ink.copy(alpha = 0.7f)
            for (i in 0 until GomokuSize) {
                val position = (i + 0.5f) * unit
                drawLine(ink, Offset(unit / 2, position), Offset(size.width - unit / 2, position), 1.dp.toPx())
                drawLine(ink, Offset(position, unit / 2), Offset(position, size.height - unit / 2), 1.dp.toPx())
            }
            for (r in listOf(3, 7, 11)) for (c in listOf(3, 7, 11)) {
                drawCircle(ink, radius = 2.5.dp.toPx(), center = Offset((c + 0.5f) * unit, (r + 0.5f) * unit))
            }
        }
        board.forEachIndexed { index, stone ->
            Box(Modifier.offset(x = cell * (index % GomokuSize), y = cell * (index / GomokuSize))
                .size(cell).clickable(enabled = enabled && stone == null) { onPoint(index) },
                contentAlignment = Alignment.Center) {
                if (stone != null) {
                    Surface(Modifier.size(cell * 0.82f).graphicsLayer {
                        val scale = if (index == lastMove) stoneScale.value else 1f
                        scaleX = scale
                        scaleY = scale
                    }, shape = CircleShape,
                        color = if (stone == GomokuStone.Black) BlackStoneColor else WhiteStoneColor,
                        border = if (stone == GomokuStone.White) BorderStroke(1.dp, Ink.copy(alpha = 0.35f)) else null,
                        shadowElevation = 2.dp) {
                        if (index == lastMove) Box(contentAlignment = Alignment.Center) {
                            Surface(Modifier.size(cell * 0.17f), shape = CircleShape,
                                color = if (stone == GomokuStone.Black) WhiteStoneColor else BlackStoneColor) {}
                        }
                    }
                }
            }
        }
    }
}
