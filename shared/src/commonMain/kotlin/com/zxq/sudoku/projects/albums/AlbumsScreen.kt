package com.zxq.sudoku.projects.albums

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import com.zxq.sudoku.games.sudoku.GamePalette

internal data class Album(val id: Int, val title: String)
internal enum class AlbumPageStyle { Plain, Paper, Dashed, Solid }
internal data class AlbumPageAppearance(val style: AlbumPageStyle = AlbumPageStyle.Plain,
                                        val palette: GamePalette = GamePalette.Island)

private val sampleNames = listOf(
    "海边日落", "山间微风", "城市散步", "周末花园", "夏日记忆",
    "雨后晴空", "湖畔时光", "旅途一角", "清晨的光", "星夜漫游",
)

internal object AlbumLibrary {
    val albums = mutableStateListOf<Album>().apply {
        addAll((1..50).map { Album(it, "${sampleNames[(it - 1) % sampleNames.size]} ${it.toString().padStart(2, '0')}") })
    }
    val savedMedia = mutableMapOf<Int, List<AlbumMedia>>()
    val savedNotes = mutableMapOf<Int, List<String>>()
    val savedTextOnLeft = mutableMapOf<Int, List<Boolean>>()
    val savedMusic = mutableMapOf<Int, String>()
    val savedAutoPlay = mutableMapOf<Int, Boolean>()
    val savedIntervalSeconds = mutableMapOf<Int, Int>()
    val savedSinglePage = mutableMapOf<Int, Boolean>()
    val savedPageAppearance = mutableMapOf<Int, List<AlbumPageAppearance>>()
    var editingId: Int? = null
}

private val coverColors = listOf(
    Color(0xFFDF9D8C) to Color(0xFF6E8BA2),
    Color(0xFFC7D8A1) to Color(0xFF648B7A),
    Color(0xFFE9C693) to Color(0xFF8DA8BE),
    Color(0xFFD3B9D9) to Color(0xFF8099A8),
    Color(0xFFA7CCCA) to Color(0xFF4F758C),
    Color(0xFFE3B9A1) to Color(0xFFA27079),
    Color(0xFFBBD3A8) to Color(0xFF628FA0),
    Color(0xFFDEC7A1) to Color(0xFF7C9B83),
    Color(0xFFADBFE0) to Color(0xFF696E9D),
    Color(0xFFE5C7C9) to Color(0xFF8D7D9E),
)

@Composable
internal fun AlbumsScreen(onBack: () -> Unit, onCreate: () -> Unit) {
    val albums = AlbumLibrary.albums
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF101010))) {
        val rowHeight = maxHeight / 5
        val cardWidth = rowHeight * 1.25f
        val viewportWidth = maxWidth
        Column(Modifier.fillMaxSize().clipToBounds()) {
            repeat(5) { row ->
                val rowAlbums = albums.filterIndexed { index, _ -> index % 5 == row }
                AlbumRow(
                    rowAlbums, row, cardWidth, rowHeight, viewportWidth,
                    onAlbumClick = {
                        if (AlbumLibrary.savedMedia.containsKey(it.id)) {
                            AlbumLibrary.editingId = it.id
                            onCreate()
                        } else selectedAlbum = it
                    })
            }
        }
        /*Row(
            Modifier.align(Alignment.TopStart).fillMaxWidth().safeDrawingPadding()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回项目首页", tint = Color.White)
            }
            Text("电子相册", style = MaterialTheme.typography.titleLarge,
                color = Color.White, fontWeight = FontWeight.Bold)
        }*/
        HorizontalFloatingToolbar(
            expanded = true,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { AlbumLibrary.editingId = null; onCreate() }
                ) {
                    /*Row(
                        Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {*/
                    Icon(Icons.Filled.Add, contentDescription = null)
//                        Text("新增相册")
//                    }
                }
            },

            floatingActionButtonPosition = FloatingToolbarHorizontalFabPosition.End,
            modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding()
                .padding(bottom = 16.dp).zIndex(1f),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回相册列表")
            }
        }

    }

    selectedAlbum?.let { album ->
        AlertDialog(
            onDismissRequest = { selectedAlbum = null },
            title = { Text(album.title) },
            text = {
                Text(AlbumLibrary.savedMedia[album.id]?.let { "已保存 ${it.size} 项媒体" }
                    ?: "这是示例相册封面，后续可在这里展示照片。")
            },
            confirmButton = { TextButton(onClick = { selectedAlbum = null }) { Text("关闭") } },
        )
    }
}

@Composable
private fun AlbumRow(
    albums: List<Album>,
    row: Int,
    cardWidth: Dp,
    rowHeight: Dp,
    viewportWidth: Dp,
    onAlbumClick: (Album) -> Unit,
) {
    if (albums.isEmpty()) return
    val cycleWidth = cardWidth * albums.size
    val cycleWidthPx = with(LocalDensity.current) { cycleWidth.toPx() }
    val repeats = (viewportWidth.value / cycleWidth.value).toInt() + 3
    val speedPxPerSecond = with(LocalDensity.current) { 18.dp.toPx() }
    var offsetPx by remember(row, cycleWidthPx) { mutableFloatStateOf(-cycleWidthPx) }
    var dragging by remember { mutableStateOf(false) }
    var resumeFraction by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(row, cycleWidthPx, speedPxPerSecond) {
        var lastFrame = 0L
        while (true) {
            val frame = withFrameNanos { it }
            val seconds = if (lastFrame == 0L) 0f else ((frame - lastFrame) / 1_000_000_000f).coerceAtMost(0.05f)
            lastFrame = frame
            if (!dragging) {
                resumeFraction = (resumeFraction + seconds / 1.8f).coerceAtMost(1f)
                val easing = resumeFraction * resumeFraction * (3f - 2f * resumeFraction)
                val direction = if (row % 2 == 0) 1f else -1f
                offsetPx = wrapOffset(offsetPx + direction * speedPxPerSecond * easing * seconds, cycleWidthPx)
            }
        }
    }
    Box(
        Modifier.fillMaxWidth().height(rowHeight).clipToBounds()
            .pointerInput(cycleWidthPx) {
                detectHorizontalDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = { dragging = false; resumeFraction = 0f },
                    onDragCancel = { dragging = false; resumeFraction = 0f },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        offsetPx = wrapOffset(offsetPx + amount, cycleWidthPx)
                    },
                )
            }) {
        // 整条重复带必须按内容宽度测量，否则 Row 只显示视口内最初的几张卡片。
        Row(Modifier.offset { IntOffset(offsetPx.roundToInt(), 0) }
            .wrapContentWidth(align = Alignment.Start, unbounded = true)) {
            repeat(repeats) {
                albums.forEach { album ->
                    AlbumCard(album, cardWidth, rowHeight, onClick = { onAlbumClick(album) })
                }
            }
        }
    }
}

private fun wrapOffset(value: Float, width: Float): Float =
    ((value % width + width) % width) - width

@Composable
private fun AlbumCard(album: Album, width: Dp, height: Dp, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(width).height(height).padding(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = coverColors[(album.id - 1) % coverColors.size].first),
    ) {
        Box(Modifier.fillMaxSize()) {
            val (sky, land) = coverColors[(album.id - 1) % coverColors.size]
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.verticalGradient(listOf(sky, land)))
                drawCircle(
                    Color.White.copy(alpha = 0.58f), radius = size.minDimension * 0.15f,
                    center = Offset(size.width * (0.25f + (album.id % 5) * 0.12f), size.height * 0.3f)
                )
                val hill = Path().apply {
                    moveTo(0f, size.height * 0.76f)
                    quadraticBezierTo(
                        size.width * 0.25f, size.height * 0.43f,
                        size.width * 0.53f, size.height * 0.73f
                    )
                    quadraticBezierTo(
                        size.width * 0.77f, size.height * 0.5f,
                        size.width, size.height * 0.69f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(hill, land.copy(alpha = 0.74f), style = Fill)
            }
            AlbumLibrary.savedMedia[album.id]?.firstOrNull()?.preview?.let { cover ->
                Image(cover, contentDescription = "${album.title}封面",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Box(
                Modifier.fillMaxWidth().align(Alignment.BottomStart)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.66f))))
                    .padding(start = 14.dp, end = 10.dp, top = 28.dp, bottom = 12.dp)
            ) {
                Text(
                    album.title, color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
