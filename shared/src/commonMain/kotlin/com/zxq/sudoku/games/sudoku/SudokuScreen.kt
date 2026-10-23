package com.zxq.sudoku.games.sudoku

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

private data class WrongEntry(val cell: Int, val digit: Int, val id: Int)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SudokuScreen(
    game: SudokuGame,
    difficultyStatistics: DifficultyStatistics,
    palette: GamePalette,
    fontSize: GameFontSize,
    onPaletteChange: (GamePalette) -> Unit,
    onFontSizeChange: (GameFontSize) -> Unit,
    onGameChange: (SudokuGame) -> Unit,
    onNoteToggle: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    var wrongEntry by remember(game.puzzle) { mutableStateOf<WrongEntry?>(null) }
    var wrongEventId by remember(game.puzzle) { mutableStateOf(0) }
    val wrongScale = remember(game.puzzle) { Animatable(1f) }
    val haptics = LocalHapticFeedback.current
    var completedUnits by remember(game.puzzle) { mutableStateOf<List<CompletedUnit>>(emptyList()) }
    var completionOrigin by remember(game.puzzle) { mutableStateOf<Int?>(null) }
    var completionEventId by remember(game.puzzle) { mutableStateOf(0) }
    val completionWave = remember(game.puzzle) { Animatable(0f) }
    val completionFade = remember(game.puzzle) { Animatable(1f) }
    LaunchedEffect(wrongEntry?.id) {
        val entry = wrongEntry ?: return@LaunchedEffect
        wrongScale.snapTo(1f)
        wrongScale.animateTo(0f, tween(550, easing = FastOutSlowInEasing))
        if (wrongEntry?.id == entry.id) wrongEntry = null
    }
    LaunchedEffect(completionEventId) {
        if (completionEventId == 0) return@LaunchedEffect
        completionWave.snapTo(0f)
        completionFade.snapTo(1f)
        completionWave.animateTo(1f, tween(850, easing = LinearEasing))
        delay(120)
        completionFade.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
        completedUnits = emptyList()
    }
    val applyGameChange: (SudokuGame) -> Unit = { next ->
        val freshUnits = newlyCompletedUnits(game.values, next.values)
        if (freshUnits.isNotEmpty()) {
            completedUnits = freshUnits
            completionOrigin = game.values.indices.firstOrNull { game.values[it] != next.values[it] }
            completionEventId++
        }
        onGameChange(next)
    }
    val enterDigit: (Int) -> Unit = { digit ->
        val next = game.enter(digit)
        val cell = game.selectedCell
        if (next.mistakes > game.mistakes && cell != null) {
            wrongEventId++
            wrongEntry = WrongEntry(cell, digit, wrongEventId)
            haptics.performHapticFeedback(HapticFeedbackType.Reject)
        }
        applyGameChange(next)
    }
    var hint by remember(game.puzzle, game.values) { mutableStateOf<SudokuHint?>(null) }
    var hintUnavailable by remember(game.puzzle, game.values) { mutableStateOf(false) }
    var hintStep by remember(game.puzzle) { mutableStateOf(0) }
    var displayedHint by remember(game.puzzle) { mutableStateOf<SudokuHint?>(null) }
    val requestHint = {
        val found = findSudokuHint(game)
        hint = found
        displayedHint = found
        hintUnavailable = found == null
        hintStep = 0
    }
    val hintCells = hint?.steps?.getOrNull(hintStep)?.cells ?: emptySet()
    MaterialTheme(colorScheme = palette.colorScheme) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val wide = maxWidth >= 820.dp && maxHeight >= 480.dp
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = if (wide) 16.dp else 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = if (wide) 960.dp else 520.dp).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onExit,) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出游戏")
                    }
                    Text(
                        "${game.puzzle.difficulty.label} · 成功 ${difficultyStatistics.wins} · 失败 ${difficultyStatistics.losses}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FilledTonalIconButton(onClick = { onGameChange(game.togglePause()) }) {
                        Icon(
                            if (game.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (game.paused) "继续游戏" else "暂停游戏",
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
//                    Text(game.puzzle.difficulty.label, color = MaterialTheme.colorScheme.primary)
                    MistakeHearts(game.mistakes)
                    Text(formatTime(game.elapsedSeconds), color = palette.ink)
                }
                if (!wide) {
                    AnimatedVisibility(
                        visible = hint != null || hintUnavailable,
                        enter = fadeIn(tween(240)) + expandVertically(tween(380, easing = FastOutSlowInEasing), expandFrom = Alignment.Top),
                        exit = fadeOut(tween(180)) + shrinkVertically(tween(300, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top),
                    ) {
                        Column {
                            HintCard(displayedHint, hintStep, { hintStep++ }, { hint = null; hintUnavailable = false })
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                if (wide) {
//                    val boardSize = minOf(maxWidth - 400.dp, maxHeight - 130.dp, 520.dp)
                    val boardSize = 520.dp
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SudokuBoard(
                            game, palette, fontSize,
                            onCellClick = { onGameChange(game.select(it)) },
                            boardSize = boardSize,
                            hintCells = hintCells,
                            wrongCell = wrongEntry?.cell,
                            wrongDigit = wrongEntry?.digit,
                            wrongScale = wrongScale.value,
                            completedUnits = completedUnits,
                            completionOrigin = completionOrigin,
                            completionProgress = completionWave.value,
                            completionFade = completionFade.value,
                        )
                        Spacer(Modifier.width(24.dp))
                        Column(Modifier.width(280.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedVisibility(
                                visible = hint != null || hintUnavailable,
                                enter = fadeIn(tween(240)) + expandVertically(tween(380, easing = FastOutSlowInEasing), expandFrom = Alignment.Top),
                                exit = fadeOut(tween(180)) + shrinkVertically(tween(300, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top),
                            ) {
                                HintCard(displayedHint, hintStep, { hintStep++ }, { hint = null; hintUnavailable = false })
                            }
                            GameControls(game, palette, fontSize, applyGameChange, enterDigit, onNoteToggle, requestHint,
                                { applyGameChange(game.revealOne()) }, wide = true)
                            Spacer(Modifier.height(12.dp))
                            GameAppearanceToolbar(palette, fontSize, onPaletteChange, onFontSizeChange)
                        }
                    }
                } else {
                    SudokuBoard(game, palette, fontSize, onCellClick = { onGameChange(game.select(it)) },
                        hintCells = hintCells, wrongCell = wrongEntry?.cell, wrongDigit = wrongEntry?.digit,
                        wrongScale = wrongScale.value, completedUnits = completedUnits,
                        completionOrigin = completionOrigin, completionProgress = completionWave.value,
                        completionFade = completionFade.value)
                    GameControls(game, palette, fontSize, applyGameChange, enterDigit, onNoteToggle, requestHint,
                        { applyGameChange(game.revealOne()) }, wide = false)
                }
            }
        }
    }
    if (!wide) {
        GameAppearanceToolbar(
            palette, fontSize, onPaletteChange, onFontSizeChange,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 12.dp),
        )
    }
    }
    if (game.paused && !game.finished) {
        AlertDialog(
            onDismissRequest = onExit,
            properties = DialogProperties(dismissOnClickOutside = false),
            title = { Text("已暂停") },
            text = { Text("准备好了就继续解题。") },
            confirmButton = {
                TextButton(onClick = { onGameChange(game.togglePause()) }) { Text("继续") }
            },
        )
    }
    if (game.finished && wrongEntry == null && completedUnits.isEmpty()) {
        AlertDialog(
            onDismissRequest = onExit,
            properties = DialogProperties(dismissOnClickOutside = false),
            title = { Text(if (game.won) "完成啦！" else "本局结束") },
            text = {
                Text(
                    if (game.won) "用时 ${formatTime(game.elapsedSeconds)}，解开了这道数独。"
                    else "已经出现 3 次错误，再来挑战一次吧。"
                )
            },
            confirmButton = {
                TextButton(onClick = onRestart) { Text("再来一局") }
            },
            dismissButton = { TextButton(onClick = onExit) { Text("返回首页") } },
        )
    }
    }
}

@Composable
private fun MistakeHearts(mistakes: Int) {
    val red = Color(0xFFD94B59)
    Row(
        modifier = Modifier.semantics { contentDescription = "剩余 ${3 - mistakes.coerceIn(0, 3)} 次错误机会" },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(3) { index ->
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = red, modifier = Modifier.fillMaxSize())
                if (index >= 3 - mistakes) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}

@Composable
private fun GameControls(
    game: SudokuGame,
    palette: GamePalette,
    fontSize: GameFontSize,
    onGameChange: (SudokuGame) -> Unit,
    onDigitClick: (Int) -> Unit,
    onNoteToggle: () -> Unit,
    onHintClick: () -> Unit,
    onRevealClick: () -> Unit,
    wide: Boolean,
) {
    Column(if (wide) Modifier.width(280.dp) else Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(if (wide) 8.dp else 20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedIconButton(
                onClick = { onGameChange(game.undo()) },
                enabled = game.canUndo && !game.paused,
                modifier = Modifier.weight(1f),
            ) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "撤回") }
            OutlinedIconToggleButton(
                checked = game.noteMode,
                onCheckedChange = { onNoteToggle() },
                enabled = !game.finished && !game.paused,
                modifier = Modifier.weight(1f),
            ) { Icon(Icons.Filled.EditNote, contentDescription = "笔记模式") }
            FilledTonalIconButton(
                onClick = { onGameChange(game.erase()) },
                enabled = !game.finished && !game.paused,
                modifier = Modifier.weight(1f),
            ) { Icon(Icons.Filled.Delete, contentDescription = "擦除") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onHintClick,
                enabled = !game.finished && !game.paused,
                modifier = Modifier.weight(1f),
            ) { Text("提示") }
            FilledTonalButton(
                onClick = onRevealClick,
                enabled = !game.finished && !game.paused,
                modifier = Modifier.weight(1f),
            ) { Text("揭晓数字") }
        }
        Spacer(Modifier.height(10.dp))
        if (wide) {
            repeat(3) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { column ->
                        GameDigit(game, palette, fontSize, row * 3 + column + 1, onDigitClick, Modifier.weight(1f))
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                (1..9).forEach { digit ->
                    GameDigit(game, palette, fontSize, digit, onDigitClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GameDigit(
    game: SudokuGame,
    palette: GamePalette,
    fontSize: GameFontSize,
    digit: Int,
    onDigitClick: (Int) -> Unit,
    modifier: Modifier,
) {
    val remaining = game.remaining(digit)
    Column(
        modifier.clickable(enabled = remaining > 0 && !game.finished && !game.paused) {
            onDigitClick(digit)
        }.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            digit.toString(),
            fontSize = fontSize.keypad,
            fontWeight = FontWeight.SemiBold,
            color = if (remaining == 0) palette.ink.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary,
        )
        Text("剩$remaining", fontSize = 10.sp, maxLines = 1, color = palette.ink)
    }
}

@Composable
private fun HintCard(
    hint: SudokuHint?,
    step: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (hint == null) "暂无可用提示" else "${hint.technique.label} · ${step + 1}/${hint.steps.size}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                hint?.steps?.getOrNull(step)?.text
                    ?: "当前局面暂时没有这个难度支持的逻辑步骤。先填写已确定的数字后再试。",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("收起") }
                if (hint != null && step < hint.steps.lastIndex) {
                    TextButton(onClick = onNext) { Text("下一步") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GameAppearanceToolbar(
    palette: GamePalette,
    fontSize: GameFontSize,
    onPaletteChange: (GamePalette) -> Unit,
    onFontSizeChange: (GameFontSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    var paletteMenuOpen by rememberSaveable { mutableStateOf(false) }
    var fontMenuOpen by rememberSaveable { mutableStateOf(false) }
    HorizontalFloatingToolbar(expanded = true, modifier = modifier) {
        Box {
            IconButton(onClick = { paletteMenuOpen = true }) {
                Icon(Icons.Filled.Palette, contentDescription = "选择主题")
            }
            DropdownMenu(expanded = paletteMenuOpen, onDismissRequest = { paletteMenuOpen = false }) {
                GamePalette.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        leadingIcon = {
                            Box(
                                Modifier.size(18.dp).background(option.swatch, CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        },
                        trailingIcon = { if (palette == option) Text("✓") },
                        onClick = {
                            onPaletteChange(option)
                            paletteMenuOpen = false
                        },
                    )
                }
            }
        }
        Box {
            IconButton(onClick = { fontMenuOpen = true }) {
                Icon(Icons.Filled.FormatSize, contentDescription = "调整字体大小")
            }
            DropdownMenu(expanded = fontMenuOpen, onDismissRequest = { fontMenuOpen = false }) {
                GameFontSize.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option.label}号") },
                        trailingIcon = { if (fontSize == option) Text("✓") },
                        onClick = {
                            onFontSizeChange(option)
                            fontMenuOpen = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun SudokuBoard(
    game: SudokuGame,
    palette: GamePalette,
    fontSize: GameFontSize,
    onCellClick: (Int) -> Unit,
    boardSize: Dp? = null,
    compact: Boolean = false,
    hintCells: Set<Int> = emptySet(),
    wrongCell: Int? = null,
    wrongDigit: Int? = null,
    wrongScale: Float = 1f,
    completedUnits: List<CompletedUnit> = emptyList(),
    completionOrigin: Int? = null,
    completionProgress: Float = 0f,
    completionFade: Float = 0f,
) {
    val selected = game.selectedCell
    val selectedValue = selected?.let { game.values[it] } ?: 0
    val compactNoteSize = when (fontSize) {
        GameFontSize.Small -> 5.sp
        GameFontSize.Medium -> 6.sp
        GameFontSize.Large -> 7.sp
    }
    val boardModifier = if (boardSize == null) {
        Modifier.widthIn(max = 520.dp).fillMaxWidth().aspectRatio(1f)
    } else {
        Modifier.size(boardSize)
    }
    Column(
        modifier = boardModifier.border(BorderStroke(2.dp, palette.ink), RoundedCornerShape(3.dp)),
    ) {
        repeat(9) { row ->
            Row(Modifier.weight(1f).fillMaxWidth()) {
                repeat(9) { column ->
                    val cell = row * 9 + column
                    val value = game.values[cell]
                    val waveAmount = completionOrigin?.let { origin ->
                        completedUnits.maxOfOrNull { completionWaveAmount(it, origin, cell, completionProgress) }
                    } ?: 0f
                    val displayedWrongDigit = wrongDigit?.takeIf { wrongCell == cell }
                    val isRelated = selected != null &&
                        (selected / 9 == row || selected % 9 == column ||
                            (selected / 27 == row / 3 && selected % 9 / 3 == column / 3))
                    val baseBackground = when {
                        selected == cell -> palette.selected
                        cell in hintCells -> palette.sameDigit
                        selectedValue != 0 && value == selectedValue -> palette.sameDigit
                        isRelated -> palette.related
                        else -> palette.board
                    }
                    val background = if (waveAmount > 0f) {
                        lerp(baseBackground, palette.selected, waveAmount * completionFade * 0.85f)
                    } else baseBackground
                    val line = if (column % 3 == 2) 2.dp else 0.5.dp
                    val bottom = if (row % 3 == 2) 2.dp else 0.5.dp
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                            .graphicsLayer {
                                val scale = 1f + 0.07f * 4f * waveAmount * (1f - waveAmount) * completionFade
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(background)
                            .drawBehind {
                                val lineWidth = line.toPx()
                                val bottomWidth = bottom.toPx()
                                if (column < 8) {
                                    val x = size.width - lineWidth / 2
                                    drawLine(palette.ink, Offset(x, 0f), Offset(x, size.height), lineWidth)
                                }
                                if (row < 8) {
                                    val y = size.height - bottomWidth / 2
                                    drawLine(palette.ink, Offset(0f, y), Offset(size.width, y), bottomWidth)
                                }
                            }
                            .semantics {
                                contentDescription = "第${row + 1}行第${column + 1}列，${if (value == 0) "空格" else value.toString()}"
                            }
                            .clickable { onCellClick(cell) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (value != 0 || displayedWrongDigit != null) {
                            Text(
                                (displayedWrongDigit ?: value).toString(),
                                modifier = Modifier.graphicsLayer {
                                    if (displayedWrongDigit != null) {
                                        scaleX = wrongScale
                                        scaleY = wrongScale
                                        alpha = wrongScale
                                    }
                                },
                                fontSize = if (compact) when (fontSize) {
                                    GameFontSize.Small -> 14.sp
                                    GameFontSize.Medium -> 17.sp
                                    GameFontSize.Large -> 20.sp
                                } else fontSize.cell,
                                fontWeight = if (game.puzzle.givens[cell] != 0) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    displayedWrongDigit != null -> Color(0xFFD94B59)
                                    game.puzzle.givens[cell] != 0 -> palette.ink
                                    else -> MaterialTheme.colorScheme.primary
                                },
                            )
                        } else if (game.notes[cell] != 0) {
                            Column(Modifier.fillMaxSize().padding(1.dp)) {
                                repeat(3) { noteRow ->
                                    Row(Modifier.weight(1f).fillMaxWidth()) {
                                        repeat(3) { noteColumn ->
                                            val digit = noteRow * 3 + noteColumn + 1
                                            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                                if (game.notes[cell] and (1 shl digit) != 0) {
                                                    Text(
                                                        digit.toString(),
                                                        fontSize = if (compact) compactNoteSize else fontSize.note,
                                                        lineHeight = if (compact) compactNoteSize else fontSize.note,
                                                        color = palette.ink,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun formatTime(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"

