package com.zxq.sudoku.games.sudoku

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun BattleScreen(
    battle: SudokuBattle,
    palette: GamePalette,
    fontSize: GameFontSize,
    onPaletteChange: (GamePalette) -> Unit,
    onFontSizeChange: (GameFontSize) -> Unit,
    onBattleChange: (SudokuBattle) -> Unit,
    onNoteToggle: (Int) -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    var paletteMenuOpen by rememberSaveable { mutableStateOf(false) }
    var fontMenuOpen by rememberSaveable { mutableStateOf(false) }
    MaterialTheme(colorScheme = palette.colorScheme) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(vertical = 40.dp)) {
                BattlePlayerPanel(
                    player = 1,
                    game = battle.first,
                    battle = battle,
                    palette = palette,
                    fontSize = fontSize,
                    modifier = Modifier.weight(1f).graphicsLayer { rotationZ = 180f },
                    onBattleChange = onBattleChange,
                    onNoteToggle = onNoteToggle,
                )
                HorizontalDivider(color = palette.ink.copy(alpha = 0.3f))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出对战")
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            formatTime(battle.elapsedSeconds),
                            modifier = Modifier.graphicsLayer { rotationZ = 180f },
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(formatTime(battle.elapsedSeconds), style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalIconButton(onClick = { onBattleChange(battle.togglePause()) }) {
                        Icon(
                            if (battle.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (battle.paused) "继续对战" else "暂停对战",
                        )
                    }
                    Box {
                        IconButton(onClick = { paletteMenuOpen = true }) {
                            Icon(Icons.Filled.Palette, contentDescription = "选择主题")
                        }
                        DropdownMenu(expanded = paletteMenuOpen, onDismissRequest = { paletteMenuOpen = false }) {
                            GamePalette.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
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
                HorizontalDivider(color = palette.ink.copy(alpha = 0.3f))
                BattlePlayerPanel(
                    player = 2,
                    game = battle.second,
                    battle = battle,
                    palette = palette,
                    fontSize = fontSize,
                    modifier = Modifier.weight(1f),
                    onBattleChange = onBattleChange,
                    onNoteToggle = onNoteToggle,
                )
            }
        }
        if (battle.paused && !battle.finished) {
            AlertDialog(
                onDismissRequest = onExit,
                properties = DialogProperties(dismissOnClickOutside = false),
                title = { Text("已暂停") },
                text = { Text("准备好了就继续对战。") },
                confirmButton = {
                    TextButton(onClick = { onBattleChange(battle.togglePause()) }) { Text("继续") }
                },
            )
        }
        if (battle.finished) {
            AlertDialog(
                onDismissRequest = onExit,
                properties = DialogProperties(dismissOnClickOutside = false),
                title = { Text(battle.winner?.let { "玩家 $it 获胜" } ?: "本局结束") },
                text = { Text(if (battle.winner == null) "双方都达到 3 次错误。" else "用时 ${formatTime(battle.elapsedSeconds)}。") },
                confirmButton = { TextButton(onClick = onRestart) { Text("再来一局") } },
                dismissButton = { TextButton(onClick = onExit) { Text("返回首页") } },
            )
        }
    }
}

@Composable
private fun BattlePlayerPanel(
    player: Int,
    game: SudokuGame,
    battle: SudokuBattle,
    palette: GamePalette,
    fontSize: GameFontSize,
    modifier: Modifier,
    onBattleChange: (SudokuBattle) -> Unit,
    onNoteToggle: (Int) -> Unit,
) {
    val canPlay = !battle.paused && !battle.finished && !game.finished
    BoxWithConstraints(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)) {
        val wide = maxWidth >= 650.dp && maxHeight >= 160.dp
        val boardSize = if (wide) {
            minOf(maxHeight - 8.dp, maxWidth - 332.dp, 420.dp)
        } else {
            minOf(maxWidth - 16.dp, (maxHeight - 116.dp).coerceAtLeast(90.dp))
        }
        val board: @Composable () -> Unit = {
            SudokuBoard(
                game = game,
                palette = palette,
                fontSize = fontSize,
                onCellClick = { cell -> onBattleChange(battle.updatePlayer(player) { it.select(cell) }) },
                boardSize = boardSize,
                compact = !wide,
            )
        }
        if (wide) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                board()
                Spacer(Modifier.size(16.dp))
                Column(Modifier.size(width = 300.dp, height = boardSize), verticalArrangement = Arrangement.Center) {
                    BattlePlayerTitle(player, game)
                    Spacer(Modifier.height(8.dp))
                    BattlePlayerControls(player, game, battle, palette, onBattleChange, onNoteToggle,
                        canPlay, wide = true)
                }
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                BattlePlayerTitle(player, game)
                Spacer(Modifier.height(2.dp))
                board()
                BattlePlayerControls(player, game, battle, palette, onBattleChange, onNoteToggle, canPlay, wide = false)
            }
        }
    }
}

@Composable
private fun BattlePlayerTitle(player: Int, game: SudokuGame) {
    Text(
        "玩家 $player · ${game.puzzle.difficulty.label}${if (game.finished && !game.won) " · 已失败" else ""}",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun BattlePlayerControls(
    player: Int,
    game: SudokuGame,
    battle: SudokuBattle,
    palette: GamePalette,
    onBattleChange: (SudokuBattle) -> Unit,
    onNoteToggle: (Int) -> Unit,
    canPlay: Boolean,
    wide: Boolean,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "错误 ${game.mistakes}/3",
            modifier = Modifier.weight(1f),
            color = if (game.mistakes > 0) Color(0xFFAF4F45) else palette.ink,
            style = MaterialTheme.typography.labelMedium,
        )
        IconButton(
            onClick = { onBattleChange(battle.updatePlayer(player) { it.undo() }) },
            enabled = canPlay && game.canUndo,
            modifier = Modifier.size(40.dp),
        ) { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "玩家 $player 撤回") }
        IconToggleButton(
            checked = game.noteMode,
            onCheckedChange = { onNoteToggle(player) },
            enabled = canPlay,
            modifier = Modifier.size(40.dp),
        ) { Icon(Icons.Filled.EditNote, contentDescription = "玩家 $player 笔记模式") }
        IconButton(
            onClick = { onBattleChange(battle.updatePlayer(player) { it.erase() }) },
            enabled = canPlay,
            modifier = Modifier.size(40.dp),
        ) { Icon(Icons.Filled.DeleteSweep, contentDescription = "玩家 $player 擦除") }
    }
    if (wide) {
        repeat(3) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { column ->
                    BattleDigit(player, game, battle, palette, onBattleChange, canPlay, row * 3 + column + 1, Modifier.weight(1f), true)
                }
            }
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            (1..9).forEach { digit ->
                BattleDigit(player, game, battle, palette, onBattleChange, canPlay, digit, Modifier.weight(1f), false)
            }
        }
    }
}

@Composable
private fun BattleDigit(
    player: Int,
    game: SudokuGame,
    battle: SudokuBattle,
    palette: GamePalette,
    onBattleChange: (SudokuBattle) -> Unit,
    canPlay: Boolean,
    digit: Int,
    modifier: Modifier,
    wide: Boolean,
) {
    val remaining = game.remaining(digit)
    Column(
        modifier.clickable(enabled = canPlay && remaining > 0) {
            onBattleChange(battle.updatePlayer(player) { it.enter(digit) })
        }.padding(vertical = if (wide) 4.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            digit.toString(),
            fontSize = if (wide) 22.sp else 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (remaining == 0) palette.ink.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary,
        )
        Text("剩$remaining", fontSize = if (wide) 10.sp else 8.sp, maxLines = 1, color = palette.ink)
    }
}
