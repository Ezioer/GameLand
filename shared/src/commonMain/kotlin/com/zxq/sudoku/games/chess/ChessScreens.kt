package com.zxq.sudoku.games.chess

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

private val BoardColor = Color(0xFFF4DCA9)
private val RedColor = Color(0xFFB64B45)
private val BlackColor = Color(0xFF40504A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChessIslandRoute(
    onBack: () -> Unit,
    onHuman: (ChessLevel) -> Unit,
    onFriend: () -> Unit,
    onPuzzle: (Int) -> Unit,
) {
    var sheet by remember { mutableStateOf<ChessMode?>(null) }
    var replay by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回游戏合集")
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("象棋小岛", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("楚河汉界，落子有方。", color = IslandGreen)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("玩法教程", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = IslandGreen)
                            IconButton(onClick = { replay++ }) { Icon(Icons.Filled.Refresh, contentDescription = "重播象棋教程") }
                        }
                        Text("先认识棋盘与走法", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        ChessTutorial(replay)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("选择玩法", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                ChessModeCard("人机对战", "从简单到困难，慢慢提高棋力", "01", Color(0xFFFFEBC5)) { sheet = ChessMode.Human }
                Spacer(Modifier.height(12.dp))
                ChessModeCard("好友对战", "同屏轮流落子，红方先行", "02", Color(0xFFE6E5FA), onFriend)
                Spacer(Modifier.height(12.dp))
                ChessModeCard("经典残局", "从精简局面出发，寻找制胜走法", "03", Color(0xFFDCEDE9)) { sheet = ChessMode.Puzzle }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    if (sheet != null) {
        ModalBottomSheet(onDismissRequest = { sheet = null }) {
            Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                Text(if (sheet == ChessMode.Human) "选择难度" else "选择残局",
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                if (sheet == ChessMode.Human) ChessLevel.entries.forEach { level ->
                    ChessChoice(level.label, when (level) {
                        ChessLevel.Easy -> "适合熟悉棋子走法"
                        ChessLevel.Medium -> "会考虑对手下一步"
                        ChessLevel.Hard -> "会预判更多回合"
                    }) { sheet = null; onHuman(level) }
                } else ChessPuzzles.entries.forEachIndexed { index, puzzle ->
                    ChessChoice(puzzle.title, puzzle.description) { sheet = null; onPuzzle(index) }
                }
            }
        }
    }
}

@Composable
private fun ChessChoice(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ChessModeCard(title: String, subtitle: String, number: String, color: Color, onClick: () -> Unit) {
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
private fun ChessTutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        step = 0
        repeat(3) { delay(2_400); step = it + 1 }
    }
    val lines = listOf("红方先行，棋子落在交叉点。", "车走直线；马走日，注意蹩马腿。", "炮隔一子才能吃子；将帅不能照面。", "保护自己的将帅，吃将或困住对方即可获胜。")
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 260.dp).fillMaxWidth()) {
                ChessBoard(
                    board = ChessRules.initialBoard(), selected = when (step) { 1 -> 9 * 9; 2 -> 7 * 9 + 1; else -> null },
                    legalTargets = emptySet(), enabled = false, onPoint = {},
                )
            }
            Spacer(Modifier.height(10.dp))
            Crossfade(targetState = step, animationSpec = tween(300), label = "象棋教程步骤") { phase ->
                Text(lines[phase], style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun ChessGameRoute(
    game: ChessGame,
    onGameChange: (ChessGame) -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    var moving by remember { mutableStateOf<ChessMove?>(null) }
    var confirmResign by remember { mutableStateOf(false) }
    var offerDraw by remember { mutableStateOf(false) }
    var drawRejected by remember { mutableStateOf(false) }
    var showRules by remember { mutableStateOf(false) }
    val computerTurn = game.mode != ChessMode.Friend && game.turn == ChessSide.Black && !game.finished
    LaunchedEffect(game, computerTurn) {
        if (computerTurn) {
            delay(350)
            val move = withContext(Dispatchers.Default) {
                ChessAi.choose(game.board, ChessSide.Black, game.level ?: ChessLevel.Medium)
            }
            if (move != null) moving = move
        }
    }
    val targets = selected?.let { game.legalMoves(it).map(ChessMove::to).toSet() } ?: emptySet()
    val title = when (game.mode) {
        ChessMode.Human -> "人机对战 · ${game.level?.label ?: ChessLevel.Medium.label}"
        ChessMode.Friend -> "好友对战"
        ChessMode.Puzzle -> ChessPuzzles.entries[game.puzzleIndex ?: 0].title
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val wide = maxWidth >= 820.dp
        val boardWidth = minOf(500.dp, maxWidth - 344.dp)
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = if (wide) 1000.dp else 620.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回象棋小岛")
                    }
                    Text(title, Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    FilledTonalButton(onClick = { showRules = true }) { Text("走法") }
                }
                Spacer(Modifier.height(12.dp))
                val checked = !game.finished && ChessRules.isInCheck(game.board, game.turn)
                Text(
                    when {
                        game.finished && game.mode == ChessMode.Puzzle && game.winner == ChessSide.Red -> "残局破解成功！"
                        game.drawn -> "双方和棋"
                        game.finished -> "${game.winner?.label}获胜"
                        moving != null -> "落子中…"
                        computerTurn -> "黑方思考中…"
                        checked -> "${game.turn.label}被将军，请先解将"
                        else -> "${game.turn.label}走棋"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (checked) RedColor else IslandGreen,
                )
                if (game.mode == ChessMode.Puzzle) {
                    Text(ChessPuzzles.entries[game.puzzleIndex ?: 0].description, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                val boardContent: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth()) {
                        ChessBoard(
                            board = game.board, selected = selected, legalTargets = targets, moving = moving,
                            enabled = !game.finished && !computerTurn && moving == null,
                            onPoint = { point ->
                                val piece = game.board[point]
                                when {
                                    selected != null && point in targets -> {
                                        moving = ChessMove(selected!!, point)
                                        selected = null
                                    }
                                    piece?.side == game.turn -> selected = point
                                    else -> selected = null
                                }
                            },
                            onMoveAnimationFinished = {
                                val move = moving
                                if (move != null) {
                                    game.move(move)?.let(onGameChange)
                                    moving = null
                                }
                            },
                        )
                        if (game.finished) ChessResultOverlay(game, Modifier.align(Alignment.Center))
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("红方已走 ${(game.history.size + 1) / 2} 步", color = RedColor, style = MaterialTheme.typography.labelLarge)
                            Text("黑方已走 ${game.history.size / 2} 步", color = BlackColor, style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onGameChange(game.undo()); selected = null },
                                enabled = !computerTurn && moving == null && !game.finished && game.history.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                            ) { Text("悔棋") }
                            OutlinedButton(onClick = { moving = null; selected = null; onRestart() },
                                modifier = Modifier.weight(1f)) { Text("重开") }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { offerDraw = true },
                                enabled = game.mode != ChessMode.Puzzle && !game.finished && !computerTurn && moving == null,
                                modifier = Modifier.weight(1f)) { Text("求和") }
                            Button(onClick = { confirmResign = true }, enabled = !game.finished && !computerTurn && moving == null,
                                modifier = Modifier.weight(1f)) { Text("认输") }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("点选己方棋子，再点亮起的交叉点落子。", style = MaterialTheme.typography.bodySmall)
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
            }
        }
    }
    if (confirmResign) AlertDialog(
        onDismissRequest = { confirmResign = false },
        title = { Text("确认认输？") },
        text = { Text("本局将由${game.turn.other().label}获胜。") },
        confirmButton = { TextButton(onClick = { onGameChange(game.resign()); confirmResign = false }) { Text("认输") } },
        dismissButton = { TextButton(onClick = { confirmResign = false }) { Text("继续下棋") } },
    )
    if (offerDraw) AlertDialog(
        onDismissRequest = { offerDraw = false },
        title = { Text("请求和棋") },
        text = { Text(if (game.mode == ChessMode.Friend) "${game.turn.label}提出和棋，${game.turn.other().label}是否同意？" else "向电脑提出和棋请求？") },
        confirmButton = { TextButton(onClick = {
            offerDraw = false
            if (game.mode == ChessMode.Friend || ChessAi.acceptsDraw(game)) onGameChange(game.agreeDraw())
            else drawRejected = true
        }) { Text(if (game.mode == ChessMode.Friend) "同意和棋" else "提出请求") } },
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
        title = { Text("象棋走法") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("车沿横线或竖线走，不可越子；炮平移同车，吃子时必须隔一子。")
                Text("马走日，前方一格被挡住时不能走；相／象走田，塞象眼不能走，也不能过河。")
                Text("仕／士斜走一格，将／帅横竖走一格，都不能出九宫；将帅不能直接照面。")
                Text("兵／卒只向前走一格，过河后还能左右走，不能后退。红方先行，不能让自己的将帅被将军。")
            }
        },
        confirmButton = { TextButton(onClick = { showRules = false }) { Text("知道了") } },
    )
}

@Composable
private fun ChessResultOverlay(game: ChessGame, modifier: Modifier = Modifier) {
    val result = when {
        game.drawn -> "和局"
        game.winner == ChessSide.Red -> "红方胜"
        else -> "黑方胜"
    }
    val color = when {
        game.drawn -> IslandGreen
        game.winner == ChessSide.Red -> RedColor
        else -> BlackColor
    }
    val scale = remember(result) { Animatable(0.15f) }
    LaunchedEffect(result) {
        scale.animateTo(1.18f, tween(440, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Surface(
        modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            alpha = ((scale.value - 0.15f) / 0.4f).coerceIn(0f, 1f)
        },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFF9E9),
        border = BorderStroke(2.dp, color),
        shadowElevation = 12.dp,
    ) {
        Text(result, Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun ChessBoard(
    board: List<ChessPiece?>,
    selected: Int?,
    legalTargets: Set<Int>,
    moving: ChessMove? = null,
    enabled: Boolean,
    onPoint: (Int) -> Unit,
    onMoveAnimationFinished: () -> Unit = {},
) {
    val progress = remember(moving) { Animatable(0f) }
    LaunchedEffect(moving) {
        if (moving != null) {
            progress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
            onMoveAnimationFinished()
        }
    }
    BoxWithConstraints(
        Modifier.fillMaxWidth().aspectRatio(0.9f).background(BoardColor, RoundedCornerShape(16.dp)),
    ) {
        val cell = maxWidth / 9
        Canvas(Modifier.fillMaxSize()) {
            val unit = size.width / 9f
            val ink = Ink.copy(alpha = 0.7f)
            for (r in 0..9) {
                val y = (r + 0.5f) * unit
                drawLine(ink, Offset(unit / 2, y), Offset(size.width - unit / 2, y), 1.4.dp.toPx())
            }
            for (c in 0..8) {
                val x = (c + 0.5f) * unit
                if (c == 0 || c == 8) drawLine(ink, Offset(x, unit / 2), Offset(x, size.height - unit / 2), 1.4.dp.toPx())
                else {
                    drawLine(ink, Offset(x, unit / 2), Offset(x, 4.5f * unit), 1.4.dp.toPx())
                    drawLine(ink, Offset(x, 5.5f * unit), Offset(x, size.height - unit / 2), 1.4.dp.toPx())
                }
            }
            for (top in listOf(0, 7)) {
                val y = (top + 0.5f) * unit
                drawLine(ink, Offset(3.5f * unit, y), Offset(5.5f * unit, y + 2 * unit), 1.4.dp.toPx())
                drawLine(ink, Offset(5.5f * unit, y), Offset(3.5f * unit, y + 2 * unit), 1.4.dp.toPx())
            }
        }
        Text("楚 河", Modifier.align(Alignment.CenterStart).padding(start = cell), color = Ink.copy(alpha = 0.8f),
            style = MaterialTheme.typography.titleMedium)
        Text("汉 界", Modifier.align(Alignment.CenterEnd).padding(end = cell), color = Ink.copy(alpha = 0.8f),
            style = MaterialTheme.typography.titleMedium)
        for (point in board.indices) {
            val piece = board[point]
            Box(
                Modifier.offset(x = cell * (point % 9), y = cell * (point / 9)).size(cell)
                    .clickable(enabled = enabled) { onPoint(point) },
                contentAlignment = Alignment.Center,
            ) {
                if (point in legalTargets && piece == null) {
                    Surface(Modifier.size(cell * 0.26f), shape = CircleShape, color = IslandGreen.copy(alpha = 0.65f)) {}
                }
                if (piece != null && point != moving?.from) {
                    val emphasis by animateFloatAsState(if (selected == point) 1.08f else 1f, tween(180), label = "选中棋子")
                    ChessPieceSurface(piece, cell * 0.80f, selected == point, point in legalTargets, emphasis)
                }
            }
        }
        val movingPiece = moving?.let { board[it.from] }
        if (moving != null && movingPiece != null) {
            Box(
                Modifier.offset(x = cell * (moving.from % 9), y = cell * (moving.from / 9))
                    .size(cell)
                    .graphicsLayer {
                        val arc = sin(progress.value.toDouble() * PI).toFloat()
                        translationX = ((moving.to % 9) - (moving.from % 9)) * cell.toPx() * progress.value
                        translationY = ((moving.to / 9) - (moving.from / 9)) * cell.toPx() * progress.value - cell.toPx() * 0.12f * arc
                        scaleX = 1f + 0.18f * arc
                        scaleY = 1f + 0.18f * arc
                    },
                contentAlignment = Alignment.Center,
            ) {
                ChessPieceSurface(movingPiece, cell * 0.80f, false, false, 1f)
            }
        }
    }
}

@Composable
private fun ChessPieceSurface(piece: ChessPiece, size: Dp, selected: Boolean, target: Boolean, scale: Float) {
    Surface(
        Modifier.size(size).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = CircleShape,
        color = if (piece.side == ChessSide.Red) Color(0xFFFFF6E6) else Color(0xFFE9E5D6),
        border = BorderStroke(if (selected || target) 3.dp else 1.5.dp,
            if (selected || target) IslandGreen else if (piece.side == ChessSide.Red) RedColor else BlackColor),
        shadowElevation = if (selected) 6.dp else 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(piece.label, color = if (piece.side == ChessSide.Red) RedColor else BlackColor,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}
