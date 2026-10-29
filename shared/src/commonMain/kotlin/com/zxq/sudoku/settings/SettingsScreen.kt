package com.zxq.sudoku.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxq.sudoku.games.sudoku.GamePalette
import com.zxq.sudoku.theme.ThemeMode

@Composable
internal fun SettingsScreen(
    mode: ThemeMode,
    palette: GamePalette,
    onModeChange: (ThemeMode) -> Unit,
    onPaletteChange: (GamePalette) -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回首页")
            }
            Text("设置", Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }
        Text("主题设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("外观模式", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { option ->
                FilterChip(selected = mode == option, onClick = { onModeChange(option) },
                    label = { Text(option.label) }, modifier = Modifier.weight(1f))
            }
        }
        Text("背景颜色", style = MaterialTheme.typography.titleMedium)
        Text("沿用数独游戏的主题颜色；深黑配色始终使用深色背景。",
            style = MaterialTheme.typography.bodyMedium)
        GamePalette.entries.chunked(2).forEach { options ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                    Card(Modifier.weight(1f).clickable { onPaletteChange(option) },
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(if (palette == option) 2.dp else 1.dp,
                            if (palette == option) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = option.background)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(Modifier.fillMaxWidth().height(38.dp),
                                contentAlignment = Alignment.CenterEnd) {
                                if (palette == option) Icon(Icons.Filled.Check,
                                    contentDescription = "已选择", tint = option.ink)
                            }
                            Text(option.label, color = option.ink,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                repeat(2 - options.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
