package com.zxq.sudoku.games.internationalchess

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zxq.sudoku.theme.Ink
import com.zxq.sudoku.theme.IslandGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val lightSquare = Color(0xFFF5EBD6)
private val darkSquare = Color(0xFF91B9A4)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun InternationalChessIslandRoute(
    onBack: () -> Unit,
    onHuman: (ChessLevel) -> Unit,
    onFriend: () -> Unit,
) {
    var chooseLevel by remember { mutableStateOf(false) }
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
                    Text("国际象棋小岛", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("守护国王，走出自己的棋路。", color = IslandGreen)
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("玩法教程", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = IslandGreen)
                        IconButton(onClick = { replay++ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "重播国际象棋教程")
                        }
                    }
                    Text("白棋先走，保护国王", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    InternationalChessTutorial(replay)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("选择玩法", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            ChessModeCard("人机对战", "简单、中级、困难，逐步挑战", "01", Color(0xFFFFEBC5)) {
                chooseLevel = true
            }
            Spacer(Modifier.height(12.dp))
            ChessModeCard("好友对战", "同屏轮流走棋，白方先行", "02", Color(0xFFE6E5FA), onFriend)
            Spacer(Modifier.height(24.dp))
        }
    }
    if (chooseLevel) ModalBottomSheet(onDismissRequest = { chooseLevel = false }) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
            Text("选择难度", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            ChessLevel.entries.forEach { level ->
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable {
                    chooseLevel = false
                    onHuman(level)
                }, shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                        Text(level.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(when (level) {
                            ChessLevel.Easy -> "认识棋子和基本走法"
                            ChessLevel.Medium -> "会考虑吃子与防守"
                            ChessLevel.Hard -> "会预判后续几步的变化"
                        }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
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
private fun InternationalChessTutorial(replay: Int) {
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(replay) {
        step = 0
        repeat(3) { delay(1250); step = it + 1 }
    }
    val game = remember(step) {
        var current = ChessGame(ChessMode.Friend)
        listOf(ChessMove(52, 36), ChessMove(12, 28), ChessMove(62, 45)).take(step).forEach {
            current = current.move(it) ?: current
        }
        current
    }
    Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFDF6), contentColor = Ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.widthIn(max = 260.dp).fillMaxWidth()) {
                InternationalChessBoard(game.position.board, game.moves.lastOrNull()?.to,
                    selected = null, legalTargets = emptySet(), enabled = false, onSquare = {})
            }
            Spacer(Modifier.height(10.dp))
            Crossfade(step, animationSpec = tween(300), label = "国际象棋教程步骤") { current ->
                Text(when (current) {
                    0 -> "白方先走。兵向前移动，首次可前进两格。"
                    1 -> "黑白双方轮流走棋，不可让自己的王受攻击。"
                    2 -> "每种棋子走法不同，点选棋子可查看合法落点。"
                    else -> "将死对方国王获胜；无合法步且未被将军则和棋。"
                }, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun InternationalChessGameRoute(
    game: ChessGame,
    onGameChange: (ChessGame) -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    var pendingPromotion by remember { mutableStateOf<List<ChessMove>?>(null) }
    var confirmResign by remember { mutableStateOf(false) }
    var offerDraw by remember { mutableStateOf(false) }
    var drawRejected by remember { mutableStateOf(false) }
    var showRules by remember { mutableStateOf(false) }
    val computerTurn = game.mode == ChessMode.Human && game.turn == ChessColor.Black && !game.finished
    val targets = remember(game, selected) { selected?.let { game.legalMoves(it).map(ChessMove::to).toSet() }.orEmpty() }
    LaunchedEffect(game.moves.size) { selected = null; pendingPromotion = null }
    LaunchedEffect(game) {
        if (computerTurn) {
            delay(420)
            val move = withContext(Dispatchers.Default) { ChessAi.choose(game) }
            move?.let { game.move(it)?.let(onGameChange) }
        }
    }
    val onSquare: (Int) -> Unit = { square ->
        val candidates = selected?.let { game.legalMoves(it).filter { move -> move.to == square } }.orEmpty()
        when {
            candidates.size > 1 -> pendingPromotion = candidates
            candidates.size == 1 -> { game.move(candidates.first())?.let(onGameChange); selected = null }
            game.position.board[square]?.color == game.turn -> selected = if (selected == square) null else square
            else -> selected = null
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val wide = maxWidth >= 820.dp
        val boardWidth = minOf(500.dp, maxWidth - 344.dp)
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = if (wide) 1000.dp else 620.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回国际象棋小岛")
                    }
                    Text(if (game.mode == ChessMode.Friend) "好友对战" else "人机对战 · ${game.level?.label}",
                        Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    FilledTonalButton(onClick = { showRules = true }) { Text("规则") }
                }
                Spacer(Modifier.height(12.dp))
                Text(when {
                    game.winner != null -> "${game.winner.label}获胜"
                    game.drawReason != null -> "和棋 · ${game.drawReason}"
                    computerTurn -> "黑方思考中…"
                    ChessRules.isInCheck(game.position.board, game.turn) -> "${game.turn.label}被将军"
                    else -> "${game.turn.label}走棋"
                }, style = MaterialTheme.typography.titleMedium, color = IslandGreen)
                Spacer(Modifier.height(10.dp))
                val boardContent: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth()) {
                        InternationalChessBoard(game.position.board, game.moves.lastOrNull()?.to,
                            selected, targets, enabled = !game.finished && !computerTurn, onSquare = onSquare)
                        if (game.finished) ChessResultOverlay(game, Modifier.align(Alignment.Center))
                    }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("白方已走 ${(game.moves.size + 1) / 2} 步", style = MaterialTheme.typography.labelLarge)
                            Text("黑方已走 ${game.moves.size / 2} 步", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { onGameChange(game.undo()) },
                                enabled = !game.finished && !computerTurn && game.history.isNotEmpty(),
                                modifier = Modifier.weight(1f)) { Text("悔棋") }
                            OutlinedButton(onClick = onRestart, modifier = Modifier.weight(1f)) { Text("重开") }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { offerDraw = true },
                                enabled = !game.finished && !computerTurn && game.moves.size >= 2,
                                modifier = Modifier.weight(1f)) { Text("求和") }
                            Button(onClick = { confirmResign = true },
                                enabled = !game.finished && !computerTurn, modifier = Modifier.weight(1f)) { Text("认输") }
                        }
                    }
                }
                if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(boardWidth)) { boardContent() }
                    Spacer(Modifier.width(24.dp))
                    Box(Modifier.width(280.dp)) { controls() }
                } else { boardContent(); controls() }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
    pendingPromotion?.let { choices ->
        AlertDialog(onDismissRequest = { pendingPromotion = null },
            title = { Text("兵升变") },
            text = { Column {
                Text("选择要升变的棋子")
                choices.forEach { move ->
                    TextButton(onClick = {
                        pendingPromotion = null
                        game.move(move)?.let(onGameChange)
                    }) { Text(move.promotion?.symbol.orEmpty(), style = MaterialTheme.typography.titleLarge) }
                }
            } },
            confirmButton = { TextButton(onClick = { pendingPromotion = null }) { Text("取消") } })
    }
    if (confirmResign) AlertDialog(onDismissRequest = { confirmResign = false },
        title = { Text("确认认输？") }, text = { Text("本局将由${game.turn.other().label}获胜。") },
        confirmButton = { TextButton(onClick = { confirmResign = false; onGameChange(game.resign()) }) { Text("认输") } },
        dismissButton = { TextButton(onClick = { confirmResign = false }) { Text("继续下棋") } })
    if (offerDraw) AlertDialog(onDismissRequest = { offerDraw = false }, title = { Text("请求和棋") },
        text = { Text(if (game.mode == ChessMode.Friend)
            "${game.turn.label}提出和棋，${game.turn.other().label}是否同意？" else "向电脑提出和棋请求？") },
        confirmButton = { TextButton(onClick = {
            offerDraw = false
            if (game.mode == ChessMode.Friend || game.position.halfmoves >= 60) onGameChange(game.agreeDraw())
            else drawRejected = true
        }) { Text(if (game.mode == ChessMode.Friend) "同意和棋" else "提出请求") } },
        dismissButton = { TextButton(onClick = { offerDraw = false }) { Text("继续下棋") } })
    if (drawRejected) AlertDialog(onDismissRequest = { drawRejected = false },
        title = { Text("求和未成功") }, text = { Text("电脑拒绝了和棋请求，请继续下棋。") },
        confirmButton = { TextButton(onClick = { drawRejected = false }) { Text("知道了") } })
    if (showRules) AlertDialog(onDismissRequest = { showRules = false }, title = { Text("国际象棋规则") },
        text = { Text("白方先走，双方轮流移动棋子。兵直进斜吃；车走直线、象走斜线、后兼具两者走法、马走日字、王每次走一格。王车易位、吃过路兵和兵升变均可使用。将死对方国王获胜；无合法步且未被将军为逼和。") },
        confirmButton = { TextButton(onClick = { showRules = false }) { Text("知道了") } })
}

@Composable
private fun ChessResultOverlay(game: ChessGame, modifier: Modifier = Modifier) {
    val result = game.winner?.let { "${it.label}胜" } ?: "和局"
    val scale = remember(result) { Animatable(0.15f) }
    LaunchedEffect(result) {
        scale.animateTo(1.18f, tween(440, easing = FastOutSlowInEasing))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow))
    }
    Surface(modifier.graphicsLayer {
        scaleX = scale.value; scaleY = scale.value
        alpha = ((scale.value - 0.15f) / 0.4f).coerceIn(0f, 1f)
    }, shape = RoundedCornerShape(24.dp), color = Color(0xFFFFF9E9),
        border = BorderStroke(2.dp, IslandGreen), shadowElevation = 12.dp) {
        Text(result, Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Ink)
    }
}

@Composable
internal fun InternationalChessBoard(
    board: List<Piece?>,
    lastMove: Int?,
    selected: Int?,
    legalTargets: Set<Int>,
    enabled: Boolean,
    onSquare: (Int) -> Unit,
) {
    val pieceScale = remember(lastMove) { Animatable(if (lastMove == null) 1f else 0.65f) }
    LaunchedEffect(lastMove) {
        if (lastMove != null) {
            pieceScale.animateTo(1.1f, tween(220, easing = FastOutSlowInEasing))
            pieceScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
        val cell = maxWidth / 8
        Column(Modifier.fillMaxSize()) {
            repeat(8) { row ->
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    repeat(8) { col ->
                        val index = row * 8 + col
                        val piece = board.getOrNull(index)
                        val base = if ((row + col) % 2 == 0) lightSquare else darkSquare
                        val color = when {
                            index == selected -> Color(0xFFFFD77A)
                            index in legalTargets -> Color(0xFFB7DDA1)
                            index == lastMove -> base.copy(alpha = 0.75f)
                            else -> base
                        }
                        Box(Modifier.weight(1f).fillMaxHeight().background(color)
                            .clickable(enabled = enabled) { onSquare(index) },
                            contentAlignment = Alignment.Center) {
                            if (piece != null) Text(pieceGlyph(piece),
                                modifier = Modifier.graphicsLayer {
                                    val scale = if (index == lastMove) pieceScale.value else 1f
                                    scaleX = scale; scaleY = scale
                                }, fontSize = (cell.value * 0.67f).sp,
                                color = Ink,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineLarge)
                            else if (index in legalTargets) Surface(Modifier.size(cell * 0.22f),
                                shape = RoundedCornerShape(50), color = Ink.copy(alpha = 0.25f)) {}
                        }
                    }
                }
            }
        }
    }
}

private fun pieceGlyph(piece: Piece): String = when (piece.color) {
    ChessColor.White -> when (piece.kind) {
        PieceKind.King -> "♔"; PieceKind.Queen -> "♕"; PieceKind.Rook -> "♖"
        PieceKind.Bishop -> "♗"; PieceKind.Knight -> "♘"; PieceKind.Pawn -> "♙"
    }
    ChessColor.Black -> when (piece.kind) {
        PieceKind.King -> "♚"; PieceKind.Queen -> "♛"; PieceKind.Rook -> "♜"
        PieceKind.Bishop -> "♝"; PieceKind.Knight -> "♞"; PieceKind.Pawn -> "♟"
    }
}
