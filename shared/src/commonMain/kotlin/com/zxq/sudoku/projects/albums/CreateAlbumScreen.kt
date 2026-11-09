package com.zxq.sudoku.projects.albums

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant
import com.zxq.sudoku.games.sudoku.GamePalette

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CreateAlbumScreen(onBack: () -> Unit) {
    val editingId = AlbumLibrary.editingId
    var title by remember(editingId) {
        mutableStateOf(AlbumLibrary.albums.firstOrNull { it.id == editingId }?.title ?: "")
    }
    val media = remember(editingId) {
        mutableStateListOf<AlbumMedia>().apply {
            addAll(editingId?.let { AlbumLibrary.savedMedia[it] }.orEmpty())
        }
    }
    val notes = remember(editingId) {
        mutableStateListOf<String>().apply {
            addAll(editingId?.let { AlbumLibrary.savedNotes[it] }.orEmpty())
        }
    }
    val textOnLeft = remember(editingId) {
        mutableStateListOf<Boolean>().apply {
            addAll(editingId?.let { AlbumLibrary.savedTextOnLeft[it] }.orEmpty())
            while (size < media.size) add(size % 2 != 0)
        }
    }
    val pageAppearance = remember(editingId) {
        mutableStateListOf<AlbumPageAppearance>().apply {
            addAll(editingId?.let { AlbumLibrary.savedPageAppearance[it] }.orEmpty())
            val existingPageCount = if (media.isEmpty()) 0 else 1 + (media.size - 1 + 2) / 3
            while (size < existingPageCount) add(AlbumPageAppearance())
        }
    }
    var singlePage by remember(editingId) { mutableStateOf(editingId?.let { AlbumLibrary.savedSinglePage[it] } ?: false) }
    var editingPage by remember { mutableStateOf<Int?>(null) }
    var musicUri by remember(editingId) { mutableStateOf(editingId?.let { AlbumLibrary.savedMusic[it] }) }
    var autoPlay by remember(editingId) { mutableStateOf(editingId?.let { AlbumLibrary.savedAutoPlay[it] } ?: false) }
    var intervalSeconds by remember(editingId) {
        mutableStateOf((editingId?.let { AlbumLibrary.savedIntervalSeconds[it] } ?: 5).coerceIn(3, 10))
    }
    var spread by remember { mutableStateOf(0) }
    var importing by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var turning by remember { mutableStateOf(false) }
    var turningForward by remember { mutableStateOf(true) }
    var draggingPage by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }
    var curlStartX by remember { mutableStateOf(0.95f) }
    var curlStartY by remember { mutableStateOf(0.75f) }
    var curlPointerY by remember { mutableStateOf(0.75f) }
    val turn = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val pickMusic = rememberAlbumMusicPicker { musicUri = it }
    val pick = rememberAlbumMediaPicker(onLoadingChange = { importing = it }) { selected ->
        media.addAll(selected)
        repeat(selected.size) { notes.add("") }
        repeat(selected.size) { textOnLeft.add(textOnLeft.size % 2 != 0) }
        val sorted = media.indices.sortedWith(compareBy<Int> { media[it].capturedAt }.thenBy { it })
        val orderedMedia = sorted.map { media[it] }
        val orderedNotes = sorted.map { notes[it] }
        val orderedPositions = sorted.map { textOnLeft[it] }
        media.clear(); media.addAll(orderedMedia)
        notes.clear(); notes.addAll(orderedNotes)
        textOnLeft.clear(); textOnLeft.addAll(orderedPositions)
        val newPageCount = 1 + (media.size - 1 + 2) / 3
        while (pageAppearance.size < newPageCount) pageAppearance.add(AlbumPageAppearance())
        spread = 0
    }
    val pageCount = if (media.isEmpty()) 0 else 1 + (media.size - 1 + 2) / 3
    val spreadCount = if (singlePage) pageCount else (pageCount + 1) / 2
    val coverDate = media.firstOrNull()?.capturedAt?.takeIf { it > 0L }
        ?.let { Instant.fromEpochMilliseconds(it).toString().take(10) }
        ?: Clock.System.now().toString().take(10)
    val coverTitle = title.ifBlank { coverDate }
    AlbumBackgroundMusic(musicUri)
    LaunchedEffect(autoPlay, intervalSeconds, spread, spreadCount, turning, editingPage, showNameDialog) {
        if (autoPlay && !turning && editingPage == null && !showNameDialog && spread + 1 < spreadCount) {
            delay(intervalSeconds * 1000L)
            if (!turning) scope.launch {
                curlStartX = if (singlePage) 0.9f else 0.95f
                curlStartY = 0.75f
                curlPointerY = curlStartY
                turningForward = true
                turning = true
                turn.snapTo(0f)
                turn.animateTo(1f, tween(720, easing = FastOutSlowInEasing))
                spread++
                turning = false
                turn.snapTo(0f)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding()
                .padding(start = 10.dp, end = 10.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "按拍摄日期排列 · 每页最多 3 项 · ${media.size} 项媒体",
                style = MaterialTheme.typography.bodySmall
            )

            if (media.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("导入媒体或音乐文件后，在这里预览相册")
                }
            } else {
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    val controlsHeight by animateDpAsState(
                        targetValue = if (autoPlay) 245.dp else 197.dp,
                        label = "相册播放设置高度"
                    )
                    val bookWidth = if (singlePage)
                        minOf(maxWidth * 0.8f, (maxHeight - controlsHeight).coerceAtLeast(0.dp) * 0.72f)
                    else minOf(maxWidth, (maxHeight - controlsHeight).coerceAtLeast(0.dp) * 1.2f)
                    val bookHeight = if (singlePage) bookWidth / 0.72f else bookWidth / 1.2f
                    Column(Modifier.width(bookWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(bookHeight).pointerInput(spread, spreadCount, singlePage) {
                            var pressX = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { start ->
                                    pressX = start.x
                                    curlStartX = (start.x / size.width).coerceIn(0f, 1f)
                                    curlStartY = (start.y / size.height).coerceIn(0f, 1f)
                                    curlPointerY = curlStartY
                                    dragProgress = 0f
                                },
                                onDragCancel = {
                                    if (draggingPage) scope.launch {
                                        turn.snapTo(dragProgress)
                                        draggingPage = false
                                        turn.animateTo(0f, tween(240))
                                        turning = false
                                    }
                                },
                                onDragEnd = {
                                    if (draggingPage) scope.launch {
                                        val finish = dragProgress >= 0.28f
                                        turn.snapTo(dragProgress)
                                        draggingPage = false
                                        turn.animateTo(if (finish) 1f else 0f, tween(420, easing = FastOutSlowInEasing))
                                        if (finish) spread += if (turningForward) 1 else -1
                                        turning = false
                                        turn.snapTo(0f)
                                    }
                                },
                                onHorizontalDrag = { change, delta ->
                                    if (!turning || draggingPage) {
                                        if (!draggingPage) {
                                            val forward = delta < 0f
                                            val pressedTurningPage = singlePage ||
                                                    (forward && pressX >= size.width / 2f) ||
                                                    (!forward && pressX < size.width / 2f)
                                            if (pressedTurningPage &&
                                                ((forward && spread + 1 < spreadCount) || (!forward && spread > 0))
                                            ) {
                                                turningForward = forward
                                                turning = true
                                                draggingPage = true
                                            }
                                        }
                                        if (draggingPage) {
                                            curlPointerY = (change.position.y / size.height).coerceIn(0f, 1f)
                                            val pageWidth = size.width * (if (singlePage) 1f else 0.5f)
                                            dragProgress = (dragProgress + (if (turningForward) -delta else delta) / pageWidth)
                                                .coerceIn(0f, 1f)
                                            change.consume()
                                        }
                                    }
                                }
                            )
                        }) {
                            BookPreview(
                                media, notes, textOnLeft, pageAppearance, coverTitle,
                                spread, singlePage, turning, turningForward,
                                if (draggingPage) dragProgress else turn.value,
                                curlStartX, curlStartY, curlPointerY,
                                onEditPage = { editingPage = it })
                        }
                        Surface(
                            modifier = Modifier.padding(top = 10.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                Modifier.padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    scope.launch {
                                        curlStartX = if (singlePage) 0.1f else 0.05f
                                        curlStartY = 0.75f
                                        curlPointerY = curlStartY
                                        turningForward = false
                                        turning = true
                                        turn.snapTo(0f)
                                        turn.animateTo(1f, tween(720, easing = FastOutSlowInEasing))
                                        spread--
                                        turning = false
                                        turn.snapTo(0f)
                                    }
                                }, enabled = spread > 0 && !turning) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("上一页")
                                }
                                Text(
                                    "${spread + 1}/$spreadCount",
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Button(onClick = {
                                    scope.launch {
                                        curlStartX = if (singlePage) 0.9f else 0.95f
                                        curlStartY = 0.75f
                                        curlPointerY = curlStartY
                                        turningForward = true
                                        turning = true
                                        turn.snapTo(0f)
                                        turn.animateTo(1f, tween(720, easing = FastOutSlowInEasing))
                                        spread++
                                        turning = false
                                        turn.snapTo(0f)
                                    }
                                }, enabled = spread + 1 < spreadCount && !turning) {
                                    Text("下一页")
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                        /* if (albumMusicSupported) {
                             TextButton(onClick = pickMusic) {
                                 Icon(Icons.Filled.MusicNote, contentDescription = null)
                                 Spacer(Modifier.width(6.dp))
                                 Text(if (musicUri == null) "添加背景音乐" else "更换背景音乐")
                             }
                         }*/
                        Row(
                            Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("单页显示", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = singlePage, enabled = !turning, onCheckedChange = {
                                singlePage = it
                                spread = 0
                            })
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("自动播放", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = autoPlay, onCheckedChange = { autoPlay = it })
                        }
                        AnimatedVisibility(
                            visible = autoPlay,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("翻页间隔", style = MaterialTheme.typography.bodyMedium)
                                    Text("${intervalSeconds} 秒", style = MaterialTheme.typography.bodyMedium)
                                }
                                Slider(
                                    value = intervalSeconds.toFloat(),
                                    onValueChange = { intervalSeconds = it.roundToInt().coerceIn(3, 10) },
                                    valueRange = 3f..10f,
                                    steps = 6
                                )
                            }
                        }
                    }
                }
            }
        }
        HorizontalFloatingToolbar(
            expanded = true,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNameDialog = true }
                ) {
                    Icon(Icons.Filled.Done, contentDescription = null)
                }
            },
            floatingActionButtonPosition = FloatingToolbarHorizontalFabPosition.End,
            modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding()
                .padding(bottom = 16.dp).zIndex(1f), content = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回相册列表")
                }
                IconButton(onClick = pick, enabled = albumMediaImportSupported) {
                    /*Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {*/
                    Icon(Icons.Default.Photo, contentDescription = "")
                    /*     Text(if (albumMediaImportSupported) "导入媒体" else "仅 Android 可导入")
                     }*/
                }
                IconButton(onClick = pickMusic) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = "添加背景音乐")
                }
                /* Spacer(Modifier.width(20.dp))
                 Button(onClick = { showNameDialog = true }, enabled = media.isNotEmpty()) {
                     Text("完成")
                 }*/
            })
        if (importing) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)).zIndex(2f),
                contentAlignment = Alignment.Center
            ) { LoadingIndicator() }
        }
    }
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("给你的相册取个名字吧") },
            text = {
                TextField(
                    title, onValueChange = { title = it },
                    label = { Text("相册名称（默认 $coverDate）") }, singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val id = editingId ?: ((AlbumLibrary.albums.maxOfOrNull { it.id } ?: 0) + 1)
                    if (editingId == null) AlbumLibrary.albums.add(Album(id, coverTitle))
                    else {
                        val index = AlbumLibrary.albums.indexOfFirst { it.id == id }
                        if (index >= 0) AlbumLibrary.albums[index] = Album(id, coverTitle)
                    }
                    AlbumLibrary.savedMedia[id] = media.toList()
                    AlbumLibrary.savedNotes[id] = notes.toList()
                    AlbumLibrary.savedTextOnLeft[id] = textOnLeft.toList()
                    if (musicUri == null) AlbumLibrary.savedMusic.remove(id)
                    else AlbumLibrary.savedMusic[id] = musicUri!!
                    AlbumLibrary.savedAutoPlay[id] = autoPlay
                    AlbumLibrary.savedIntervalSeconds[id] = intervalSeconds
                    AlbumLibrary.savedSinglePage[id] = singlePage
                    AlbumLibrary.savedPageAppearance[id] = pageAppearance.toList()
                    AlbumLibrary.editingId = null
                    showNameDialog = false
                    onBack()
                }, enabled = media.isNotEmpty()) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("取消") } },
        )
    }
    editingPage?.let { pageIndex ->
        val current = pageAppearance.getOrNull(pageIndex) ?: AlbumPageAppearance()
        var applyAllPages by remember(pageIndex) { mutableStateOf(false) }
        fun applyAppearance(next: AlbumPageAppearance) {
            if (applyAllPages) pageAppearance.indices.forEach { pageAppearance[it] = next }
            else pageAppearance[pageIndex] = next
        }
        ModalBottomSheet(onDismissRequest = { editingPage = null }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("编辑第 ${pageIndex + 1} 页", style = MaterialTheme.typography.titleLarge)
                Text("页面样式", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlbumPageStyle.entries.forEach { style ->
                        val label = when (style) {
                            AlbumPageStyle.Plain -> "默认"
                            AlbumPageStyle.Paper -> "仿纸张"
                            AlbumPageStyle.Dashed -> "虚线"
                            AlbumPageStyle.Solid -> "实线"
                        }
                        Column(
                            Modifier.weight(1f).clickable { applyAppearance(current.copy(style = style)) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PageAppearanceThumbnail(
                                appearance = current.copy(style = style),
                                selected = current.style == style,
                                modifier = Modifier.fillMaxWidth().height(64.dp)
                            )
                            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
                Text("页面背景", style = MaterialTheme.typography.titleMedium)
                GamePalette.entries.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { palette ->
                            Column(
                                Modifier.weight(1f).clickable { applyAppearance(current.copy(palette = palette)) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PageAppearanceThumbnail(
                                    appearance = current.copy(palette = palette),
                                    selected = current.palette == palette,
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                )
                                Text(palette.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("应用到所有页面", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = applyAllPages, onCheckedChange = { checked ->
                        applyAllPages = checked
                        if (checked) pageAppearance.indices.forEach { pageAppearance[it] = current }
                    })
                }
            }
        }
    }
}

@Composable
private fun BookPreview(
    media: List<AlbumMedia>, notes: MutableList<String>, textOnLeft: MutableList<Boolean>,
    appearances: List<AlbumPageAppearance>, coverTitle: String, spread: Int, singlePage: Boolean,
    turning: Boolean, turningForward: Boolean, turnValue: Float,
    curlStartX: Float, curlStartY: Float, curlPointerY: Float,
    onEditPage: (Int) -> Unit
) {
    fun page(step: Int, left: Boolean): Int? = when {
        singlePage -> step
        else -> 2 * step + if (left) 0 else 1
    }

    val pageCount = appearances.size
    val pressOnPage = (if (singlePage) {
        if (turningForward) curlStartX else 1f - curlStartX
    } else {
        if (turningForward) (curlStartX - 0.5f) * 2f else 1f - curlStartX * 2f
    }).coerceIn(0f, 1f)
    Box(Modifier.fillMaxSize()) {
        if (singlePage) {
            val basePage = if (turning) spread + if (turningForward) 1 else -1 else spread
            BookPage(
                media, notes, textOnLeft, basePage, appearances, coverTitle, onEditPage,
                Modifier.fillMaxSize()
            )
            if (turning) {
                BookPage(
                    media, notes, textOnLeft, spread, appearances, coverTitle, onEditPage,
                    Modifier.fillMaxSize().pageCurl(
                        turnValue, turningForward, pressOnPage, curlStartY, curlPointerY
                    )
                )
            }
        } else {
            Row(Modifier.fillMaxSize().background(Color(0xFF80776F), RoundedCornerShape(8.dp)).padding(4.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    BookPage(
                        media, notes, textOnLeft,
                        page(spread - if (turning && !turningForward) 1 else 0, true),
                        appearances, coverTitle, onEditPage, Modifier.fillMaxSize().padding(end = 6.dp)
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    BookPage(
                        media, notes, textOnLeft,
                        page(spread + if (turning && turningForward) 1 else 0, false)
                            ?.takeIf { it < pageCount },
                        appearances, coverTitle, onEditPage, Modifier.fillMaxSize().padding(start = 6.dp)
                    )
                }
            }
            Box(
                Modifier.align(Alignment.Center).fillMaxSize().padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.fillMaxWidth(0.015f).fillMaxHeight()
                        .background(Color(0xFF494641), RoundedCornerShape(10.dp))
                )
            }
            if (turning) {
                val turningPage = page(spread, left = !turningForward)
                val backPage = page(
                    spread + if (turningForward) 1 else -1,
                    left = turningForward
                )
                val turningSide = if (turningForward) Alignment.CenterEnd else Alignment.CenterStart
                val turningPadding = Modifier.padding(
                    start = if (turningForward) 6.dp else 0.dp,
                    end = if (turningForward) 0.dp else 6.dp
                )
                Box(
                    Modifier.fillMaxSize().padding(4.dp).pageCurl(
                        turnValue, turningForward, pressOnPage, curlStartY, curlPointerY,
                        spreadMode = true, backOnly = true
                    )
                ) {
                    BookPage(
                        media, notes, textOnLeft, backPage, appearances, coverTitle, onEditPage,
                        Modifier.align(turningSide).fillMaxWidth(0.5f).fillMaxHeight()
                            .then(turningPadding).graphicsLayer { scaleX = -1f }
                    )
                }
                Box(
                    Modifier.fillMaxSize().padding(4.dp).pageCurl(
                        turnValue, turningForward, pressOnPage, curlStartY, curlPointerY,
                        spreadMode = true
                    )
                ) {
                    BookPage(
                        media, notes, textOnLeft, turningPage, appearances, coverTitle, onEditPage,
                        Modifier.align(turningSide).fillMaxWidth(0.5f).fillMaxHeight()
                            .then(turningPadding)
                    )
                }
            }
        }
    }
}

private fun Modifier.pageCurl(
    progress: Float, forward: Boolean, pressX: Float, pressY: Float, pointerY: Float,
    spreadMode: Boolean = false, backOnly: Boolean = false
): Modifier =
    drawWithContent {
        val p = progress.coerceIn(0f, 1f)
        val width = if (spreadMode) size.width / 2f else size.width
        val height = size.height
        if (p <= 0f) {
            if (!backOnly) drawContent()
            return@drawWithContent
        }
        if (p >= 1f) {
            if (spreadMode && backOnly) {
                scale(scaleX = -1f, scaleY = 1f, pivot = Offset(width, 0f)) {
                    this@drawWithContent.drawContent()
                }
            }
            return@drawWithContent
        }

        // 把按下的高度作为页边起点，手指高度决定折角；中线按两点的垂直平分线计算。
        val originY = pressY.coerceIn(0f, 1f) * height
        val lift = sin(p * 3.1415927f)
        val tipX = width * (1f - 2f * p - (1f - pressX) * 0.35f * lift)
        val tipY = height * (pressY +
                (pointerY - pressY + (0.5f - pressY) * 0.8f) * lift).coerceIn(0f, 1f)
        val dx = width - tipX
        val dy = originY - tipY
        val constant = dx * (width + tipX) / 2f + dy * (originY + tipY) / 2f
        val corners = listOf(Offset(0f, 0f), Offset(width, 0f), Offset(width, height), Offset(0f, height))
        fun side(point: Offset) = dx * point.x + dy * point.y - constant
        val frontPoints = mutableListOf<Offset>()
        var previous = corners.last()
        for (current in corners) {
            val previousSide = side(previous)
            val currentSide = side(current)
            if ((previousSide <= 0f) != (currentSide <= 0f)) {
                val fraction = previousSide / (previousSide - currentSide)
                frontPoints += Offset(
                    previous.x + (current.x - previous.x) * fraction,
                    previous.y + (current.y - previous.y) * fraction
                )
            }
            if (currentSide <= 0f) frontPoints += current
            previous = current
        }
        val tolerance = (width * width + height * height) * 0.0001f
        val creaseEdge = frontPoints.indices.firstOrNull { index ->
            val a = frontPoints[index]
            val b = frontPoints[(index + 1) % frontPoints.size]
            abs(side(a)) < tolerance && abs(side(b)) < tolerance &&
                    (a - b).getDistance() > 0.5f
        }
        if (creaseEdge == null) {
            if (!backOnly) drawContent()
            return@drawWithContent
        }
        val a = frontPoints[creaseEdge]
        val b = frontPoints[(creaseEdge + 1) % frontPoints.size]
        val tip = Offset(if (spreadMode) tipX else tipX.coerceIn(0f, width), tipY)
        val bend = min(width, height) * 0.07f * lift
        fun place(point: Offset) = Offset(
            if (forward) point.x + (if (spreadMode) width else 0f) else width - point.x,
            point.y
        )

        val start = place(a)
        val end = place(b)
        val curlTip = place(tip)
        val firstControl = place(Offset((a.x + tip.x) / 2f + bend, (a.y + tip.y) / 2f))
        val secondControl = place(Offset((tip.x + b.x) / 2f, (tip.y + b.y) / 2f + bend))
        val front = Path().apply {
            val first = place(frontPoints.first())
            moveTo(first.x, first.y)
            for (index in frontPoints.indices) {
                val next = place(frontPoints[(index + 1) % frontPoints.size])
                if (index == creaseEdge) {
                    quadraticBezierTo(firstControl.x, firstControl.y, curlTip.x, curlTip.y)
                    quadraticBezierTo(secondControl.x, secondControl.y, end.x, end.y)
                } else lineTo(next.x, next.y)
            }
            close()
        }
        if (!backOnly) clipPath(front) { this@drawWithContent.drawContent() }

        if (spreadMode && backOnly) {
            val foldedPoints = mutableListOf<Offset>()
            var last = corners.last()
            for (current in corners) {
                val lastSide = side(last)
                val currentSide = side(current)
                if ((lastSide >= 0f) != (currentSide >= 0f)) {
                    val fraction = lastSide / (lastSide - currentSide)
                    foldedPoints += Offset(
                        last.x + (current.x - last.x) * fraction,
                        last.y + (current.y - last.y) * fraction
                    )
                }
                if (currentSide >= 0f) foldedPoints += current
                last = current
            }
            val normalLengthSquared = dx * dx + dy * dy
            fun reflect(point: Offset): Offset {
                val distance = side(point) / normalLengthSquared
                return Offset(point.x - 2f * distance * dx, point.y - 2f * distance * dy)
            }

            val foldedBack = Path().apply {
                foldedPoints.forEachIndexed { index, point ->
                    val reflected = place(reflect(point))
                    if (index == 0) moveTo(reflected.x, reflected.y)
                    else lineTo(reflected.x, reflected.y)
                }
                close()
            }
            clipPath(foldedBack) {
                val angle = atan2(end.y - start.y, end.x - start.x) * 57.29578f
                rotate(angle, pivot = start) {
                    scale(scaleX = 1f, scaleY = -1f, pivot = start) {
                        rotate(-angle, pivot = start) {
                            this@drawWithContent.drawContent()
                        }
                    }
                }
                drawRect(Color(0xFFFFF8EA).copy(alpha = 0.14f))
                // 阴影沿反射后的纸张外缘走，裁剪区域会截去尚未翻起的部分。
                drawLine(
                    Color.Black.copy(alpha = 0.16f * lift),
                    start = place(reflect(Offset(width, 0f))),
                    end = place(reflect(Offset(width, height))),
                    strokeWidth = 5.dp.toPx()
                )
            }
            return@drawWithContent
        }

        if (spreadMode) {
            val crease = Path().apply {
                moveTo(start.x, start.y)
                quadraticBezierTo(firstControl.x, firstControl.y, curlTip.x, curlTip.y)
                quadraticBezierTo(secondControl.x, secondControl.y, end.x, end.y)
            }
            clipRect(
                left = if (forward) width else 0f,
                top = 0f, right = if (forward) size.width else width, bottom = height
            ) {
                drawPath(
                    crease, Color.Black.copy(alpha = 0.16f * lift),
                    style = Stroke(width = 5.dp.toPx())
                )
            }
            return@drawWithContent
        }

        // 单页模式沿用原有的局部折角。
        val back = Path().apply {
            moveTo(start.x, start.y)
            quadraticBezierTo(firstControl.x, firstControl.y, curlTip.x, curlTip.y)
            quadraticBezierTo(secondControl.x, secondControl.y, end.x, end.y)
            lineTo(start.x, start.y)
            close()
        }
        clipPath(back) {
            drawPath(back, Color(0xFFFFF8EA))
            val angle = atan2(end.y - start.y, end.x - start.x) * 57.29578f
            rotate(angle, pivot = start) {
                scale(scaleX = 1f, scaleY = -1f, pivot = start) {
                    rotate(-angle, pivot = start) {
                        this@drawWithContent.drawContent()
                    }
                }
            }
            drawRect(Color(0xFFFFF8EA).copy(alpha = 0.55f))
        }
        val crease = Path().apply {
            moveTo(start.x, start.y)
            quadraticBezierTo(firstControl.x, firstControl.y, curlTip.x, curlTip.y)
            quadraticBezierTo(secondControl.x, secondControl.y, end.x, end.y)
        }
        drawPath(
            crease, Color.Black.copy(alpha = 0.16f * sin(p * 3.1415927f)),
            style = Stroke(width = 5.dp.toPx())
        )
    }

private fun Modifier.albumPagePattern(appearance: AlbumPageAppearance): Modifier = drawBehind {
    val ink = appearance.palette.ink.copy(alpha = 0.35f)
    when (appearance.style) {
        AlbumPageStyle.Paper -> {
            var y = 12.dp.toPx()
            while (y < size.height) {
                drawLine(
                    ink.copy(alpha = 0.18f), androidx.compose.ui.geometry.Offset(0f, y),
                    androidx.compose.ui.geometry.Offset(size.width, y), 1.dp.toPx()
                )
                y += 18.dp.toPx()
            }
        }

        AlbumPageStyle.Dashed -> drawRect(
            ink, style = Stroke(
                2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
            )
        )

        AlbumPageStyle.Solid -> drawRect(ink, style = Stroke(2.dp.toPx()))
        AlbumPageStyle.Plain -> Unit
    }
}

@Composable
private fun PageAppearanceThumbnail(
    appearance: AlbumPageAppearance, selected: Boolean, modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier.border(
            2.dp, if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant, shape
        ).padding(3.dp)
            .clip(shape).background(appearance.palette.background).albumPagePattern(appearance),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(24.dp).background(
                appearance.palette.swatch.copy(alpha = 0.75f),
                RoundedCornerShape(4.dp)
            )
        )
    }
}

@Composable
private fun BookPage(
    media: List<AlbumMedia>, notes: MutableList<String>, textOnLeft: MutableList<Boolean>,
    pageIndex: Int?, appearances: List<AlbumPageAppearance>, coverTitle: String,
    onEditPage: (Int) -> Unit, modifier: Modifier = Modifier
) {
    val appearance = pageIndex?.let { appearances.getOrNull(it) } ?: AlbumPageAppearance()
    Card(
        modifier, shape = RoundedCornerShape(4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = appearance.palette.background)
    ) {
        Box(Modifier.fillMaxSize().albumPagePattern(appearance)) {
            if (pageIndex == 0 && media.isNotEmpty()) {
                Column(Modifier.fillMaxSize().padding(8.dp)) {
                    Text(
                        coverTitle, style = MaterialTheme.typography.titleMedium,
                        color = appearance.palette.ink,
                        modifier = Modifier.fillMaxWidth().padding(end = 40.dp), maxLines = 2
                    )
                    media.first().preview?.let { cover ->
                        Image(
                            cover, contentDescription = "相册封面",
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else if (pageIndex != null) {
                val start = 1 + (pageIndex - 1) * 3
                Column(
                    Modifier.fillMaxSize().padding(top = 36.dp, start = 4.dp, end = 4.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { slot ->
                        val index = start + slot
                        if (index < media.size) {
                            AlbumPageItem(
                                media[index], notes[index], index, textOnLeft[index],
                                onNoteChange = { notes[index] = it },
                                onSwap = { textOnLeft[index] = !textOnLeft[index] },
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                }
            }
            if (pageIndex != null && pageIndex < appearances.size) {
                IconButton(onClick = { onEditPage(pageIndex) }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Filled.Edit, contentDescription = "编辑当前页样式", tint = appearance.palette.ink)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlbumPageItem(
    item: AlbumMedia, note: String, index: Int, textOnLeft: Boolean,
    onNoteChange: (String) -> Unit, onSwap: () -> Unit, modifier: Modifier = Modifier
) {
    var showNoteDialog by remember(item.uri, index) { mutableStateOf(false) }
    var draftNote by remember(item.uri, index) { mutableStateOf(note) }
    var swapDrag by remember(item.uri, index) { mutableStateOf(0f) }
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val ratio = item.aspectRatio.coerceIn(0.4f, 3f)
        val noteWidth = if (note.isBlank()) 20.dp else maxWidth * 0.25f
        val imageWidth = minOf((maxWidth - noteWidth - 6.dp).coerceAtLeast(0.dp), maxHeight * ratio)
        val itemHeight = imageWidth / ratio
        val imageFirst = !textOnLeft
        val polygonPath = when (((index + item.uri.hashCode()) and Int.MAX_VALUE) % 3) {
            0 -> MaterialShapes.Square
            1 -> MaterialShapes.Cookie9Sided
            else -> MaterialShapes.Clover4Leaf
        }.toPath()
        val imageShape = remember(polygonPath) {
            GenericShape { size, _ ->
                addPath(polygonPath)
                val bounds = getBounds()
                val scale = minOf(size.width / bounds.width, size.height / bounds.height)
                transform(Matrix().apply { scale(scale, scale) })
                translate(size.center - getBounds().center)
            }
        }
        val image: @Composable () -> Unit = {
            Box(Modifier.width(imageWidth).height(itemHeight).clickable(enabled = item.isVideo) {
                openAlbumVideo(item.uri)
            }, contentAlignment = Alignment.Center) {
                item.preview?.let {
                    Image(
                        it, contentDescription = if (item.isVideo) "视频缩略图" else "相册照片",
                        modifier = Modifier.fillMaxSize().clip(imageShape), contentScale = ContentScale.Crop
                    )
                }
                if (item.isVideo) Icon(Icons.Filled.PlayArrow, contentDescription = "播放视频")
            }
        }
        val caption: @Composable () -> Unit = {
            Box(
                Modifier.width(noteWidth)
                    .height(if (note.isBlank()) 20.dp else itemHeight)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
                    .clickable {
                        draftNote = note
                        showNoteDialog = true
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (note.isBlank()) {
                    Icon(Icons.Filled.Title, contentDescription = "添加文字")
                } else {
                    Text(
                        note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Row(
            modifier = Modifier.pointerInput(index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { swapDrag = 0f },
                    onDragEnd = {
                        if (abs(swapDrag) > 24f) onSwap()
                        swapDrag = 0f
                    },
                    onDragCancel = { swapDrag = 0f },
                    onDrag = { change, drag ->
                        swapDrag += drag.x
                        change.consume()
                    }
                )
            }.graphicsLayer {
                scaleX = if (swapDrag == 0f) 1f else 1.04f
                scaleY = if (swapDrag == 0f) 1f else 1.04f
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (imageFirst) {
                image(); caption()
            } else {
                caption(); image()
            }
        }
    }
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("编辑相册文字") },
            text = {
                TextField(
                    value = draftNote,
                    onValueChange = { draftNote = it },
                    label = { Text("写点什么") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onNoteChange(draftNote.trim())
                    showNoteDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("取消") }
            }
        )
    }
}
