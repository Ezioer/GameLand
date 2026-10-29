package com.zxq.sudoku.games.tetris

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
internal fun TetrisMatchSheet(match: TetrisMatch, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("好友对战", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text((if (match.nfcAvailable) "等待发现好友后，将手机背面轻触即可配对并自动开局。" else
                    "正在寻找好友，找到后选择并核对连接码。") +
                    "消除几行，就给好友增加几行干扰；先见顶的一方落败。",
                    style = MaterialTheme.typography.bodyMedium)
                if (match.phase == MatchPhase.Disconnected ||
                    (match.phase == MatchPhase.Idle && match.error != null)) {
                    Button(onClick = match::pair, modifier = Modifier.fillMaxWidth()) { Text("重新连接") }
                }
                if (match.phase == MatchPhase.Searching && !match.showFallback) {
                    OutlinedButton(onClick = match::useConnectionCode, modifier = Modifier.fillMaxWidth()) {
                        Text("改用连接码")
                    }
                }
                when (match.phase) {
                    MatchPhase.Searching -> {
                        PairingAnimation(match.pairingMessage ?: "正在寻找附近好友", match.nfcAvailable)
                        if (match.showFallback) {
                            Text("选择好友后，核对两台手机显示的连接码")
                            match.peers.forEach { peer ->
                                OutlinedButton(onClick = { match.connect(peer.id) },
                                    modifier = Modifier.fillMaxWidth()) { Text("连接 ${peer.name}") }
                            }
                        }
                    }
                    MatchPhase.Connected -> PairingAnimation("配对成功，正在开局…", match.nfcAvailable)
                    MatchPhase.Disconnected -> Text("连接已断开，请重新配对")
                    MatchPhase.Idle -> if (match.error == null)
                        PairingAnimation("正在启动附近连接…", match.nfcAvailable)
                    MatchPhase.Connecting -> Column(Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LoadingIndicator()
                        Text("正在连接好友…")
                    }
                    else -> Unit
                }
                match.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("取消") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    match.verification?.let { request ->
        AlertDialog(onDismissRequest = match::reject,
            title = { Text("确认连接 ${request.name}") },
            text = { Text("请与好友核对连接码：${request.digits}\n两台手机显示一致后，各自点击确认。") },
            confirmButton = { TextButton(onClick = match::accept) { Text("确认") } },
            dismissButton = { TextButton(onClick = match::reject) { Text("取消") } })
    }
}

@Composable
private fun PairingAnimation(message: String, nfcAvailable: Boolean) {
    val pulse = rememberInfiniteTransition(label = "NFC 配对")
    val scale = pulse.animateFloat(0.8f, 1.25f,
        infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "接触波纹")
    val opacity = pulse.animateFloat(0.25f, 0.7f,
        infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "波纹透明度")
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(76.dp).graphicsLayer { scaleX = scale.value; scaleY = scale.value;
                alpha = opacity.value }.background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
            Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center) {
                Text(if (nfcAvailable) "NFC" else "连接", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        AnimatedContent(targetState = message, label = "配对状态") { stage ->
            Text(stage, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
internal fun TetrisMatchResult(match: TetrisMatch, local: TetrisGame, onExit: () -> Unit) {
    val result = match.result
    if (result != null || match.phase == MatchPhase.Disconnected) {
        AlertDialog(onDismissRequest = {},
            title = { Text(when (result) {
                MatchResult.Win -> "你赢了！"
                MatchResult.Lose -> "好友获胜"
                MatchResult.Draw -> "平局"
                null -> "连接已断开"
            }) },
            text = { Text(if (result == null) match.error ?: "本局对战已结束" else
                "你消除 ${local.lines} 行 · 好友消除 ${match.remoteGame?.lines ?: 0} 行") },
            confirmButton = { TextButton(onClick = onExit) { Text("返回小岛") } })
    }
}
