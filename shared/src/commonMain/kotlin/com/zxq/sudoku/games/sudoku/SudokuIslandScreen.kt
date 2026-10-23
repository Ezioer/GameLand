package com.zxq.sudoku.games.sudoku

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zxq.sudoku.theme.IslandGreen
import com.zxq.sudoku.theme.Ink
import kotlinx.coroutines.delay

private enum class SudokuMode { Classic, Battle }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SudokuIslandRoute(
    onBack: () -> Unit,
    onStartClassic: (SudokuDifficulty) -> Unit,
    onStartBattle: (SudokuDifficulty) -> Unit,
) {
    var showDifficultySheet by rememberSaveable { mutableStateOf(false) }
    var selectedMode by rememberSaveable { mutableStateOf(SudokuMode.Classic) }
    SudokuIslandScreen(
        onBack = onBack,
        onClassicClick = { selectedMode = SudokuMode.Classic; showDifficultySheet = true },
        onBattleClick = { selectedMode = SudokuMode.Battle; showDifficultySheet = true },
    )
    if (showDifficultySheet) {
        ModalBottomSheet(onDismissRequest = { showDifficultySheet = false }) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                Text("选择难度", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("每局都有唯一解，按自己的节奏开始。", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(18.dp))
                SudokuDifficulty.entries.forEach { difficulty ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable {
                            showDifficultySheet = false
                            if (selectedMode == SudokuMode.Classic) onStartClassic(difficulty)
                            else onStartBattle(difficulty)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(difficulty.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(difficulty.description, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${difficulty.clues} 提示", style = MaterialTheme.typography.labelMedium, color = IslandGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NoteTutorialDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("笔记模式怎么玩") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("不确定答案时，先选中空格，开启笔记，再点下方数字，将它记为候选数。再次点同一数字可移除笔记。")
                Text("笔记不会填入答案。确定后关闭笔记模式，再点数字正式填写；撤回和擦除也能修改笔记。")
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("开启笔记") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("稍后再说") } },
    )
}

@Composable
internal fun BeginnerTutorialDialog(onFinished: () -> Unit) {
    AlertDialog(
        onDismissRequest = onFinished,
        title = { Text("数独玩法") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("每行、每列和每个 3 × 3 九宫格，都要填入 1 到 9，且数字不能重复。")
                Text("先点一个空格，再点下方数字填入。填错累计 3 次，本局结束；可以用撤回或擦除调整。")
                Text("不确定答案时，打开「笔记」记录候选数字；再点一次关闭笔记，便能正式填数。")
            }
        },
        confirmButton = { TextButton(onClick = onFinished) { Text("开始游戏") } },
    )
}

@Composable
private fun SudokuIslandScreen(onBack: () -> Unit, onClassicClick: () -> Unit, onBattleClick: () -> Unit) {
    var tutorialReplayKey by rememberSaveable { mutableStateOf(0) }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 46.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 640.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回游戏合集")
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("数独小岛", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("数独小岛 ，经典与对战", style = MaterialTheme.typography.bodyLarge, color = IslandGreen)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "玩法教程",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge,
                                color = IslandGreen,
                            )
                            IconButton(onClick = { tutorialReplayKey++ }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "重播玩法教程")
                            }
                        }
                        Text("看懂数独的三个规则", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SudokuTutorialPreview(tutorialReplayKey)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("探索玩法", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                FeatureCard("经典数独", "按自己的节奏，专注每一步", "01", Color(0xFFFFEBC5), onClassicClick)
                Spacer(Modifier.height(12.dp))
                FeatureCard("好友对战", "一起挑战，看谁先解开谜题", "02", Color(0xFFE6E5FA), onBattleClick)
                /*  Spacer(Modifier.height(12.dp))
                  FeatureCard("成长记录", "回顾每一次小小的进步", "03", Color(0xFFDCEDE9))*/
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SudokuTutorialPreview(replayKey: Int) {
    var unit by remember { mutableStateOf(TutorialUnit.Box) }
    var revealedCount by remember { mutableStateOf(0) }
    LaunchedEffect(replayKey) {
        TutorialUnit.entries.forEach { nextUnit ->
            unit = nextUnit
            revealedCount = 0
            delay(350)
            repeat(9) { step ->
                revealedCount = step + 1
                delay(180)
            }
            delay(900)
        }
    }
    val selectedCells = tutorialCells(unit)
    Surface(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFFFDF6),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(unit.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("$revealedCount / 9", style = MaterialTheme.typography.labelLarge, color = IslandGreen)
            }
            Column(
                Modifier.fillMaxWidth().aspectRatio(1f)
                    .border(BorderStroke(2.dp, Ink), RoundedCornerShape(4.dp)),
            ) {
                repeat(9) { row ->
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        repeat(9) { column ->
                            val cell = row * 9 + column
                            val position = selectedCells.indexOf(cell)
                            val highlighted = position >= 0
                            val revealed = highlighted && position < revealedCount
                            val background by animateColorAsState(
                                targetValue = when {
                                    revealed -> Color(0xFFFFD77A)
                                    highlighted -> Color(0xFFFFF0C9)
                                    else -> Color.White
                                },
                                animationSpec = tween(220),
                                label = "教程格子颜色",
                            )
                            Box(
                                Modifier.weight(1f).fillMaxHeight().background(background)
                                    .drawBehind {
                                        if (column < 8) {
                                            val lineWidth = (if (column % 3 == 2) 1.5.dp else 0.5.dp).toPx()
                                            val x = size.width - lineWidth / 2
                                            drawLine(Ink, Offset(x, 0f), Offset(x, size.height), lineWidth)
                                        }
                                        if (row < 8) {
                                            val lineWidth = (if (row % 3 == 2) 1.5.dp else 0.5.dp).toPx()
                                            val y = size.height - lineWidth / 2
                                            drawLine(Ink, Offset(0f, y), Offset(size.width, y), lineWidth)
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    tutorialValue(cell).toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (revealed) FontWeight.Bold else FontWeight.Normal,
                                    color = if (revealed) Ink else Ink.copy(alpha = 0.3f),
                                )
                            }
                        }
                    }
                }
            }
            Text(unit.rule, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    number: String,
    color: Color,
    onClick: (() -> Unit)? = null,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.65f)) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(number, fontWeight = FontWeight.Bold, color = IslandGreen)
                }
            }
            Column(Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

