package com.zxq.sudoku.projects.drawtolife

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

private data class FishKind(
    val name: String, val color: Color, val tail: Float, val bodyHeight: Float, val scene: Int = 0
)

private data class LivingFish(val kind: FishKind, val strokes: List<List<Offset>>)

private val kinds = listOf(
    FishKind("公子小丑鱼", Color(0xFFFF9866), 0.13f, 0.17f),
    FishKind("金鱼", Color(0xFFFFBA57), 0.25f, 0.20f),
    FishKind("蓝倒吊", Color(0xFF448FD4), 0.17f, 0.15f),
    FishKind("黄倒吊", Color(0xFFF6D95F), 0.15f, 0.19f),
    FishKind("鲸鱼（哺乳动物）", Color(0xFF83AFC1), 0.12f, 0.20f),
    FishKind("鲨鱼", Color(0xFF8FA7B3), 0.19f, 0.15f),
    FishKind("金枪鱼", Color(0xFF5C94B3), 0.23f, 0.11f),
    FishKind("长吻海马", Color(0xFFE7A56A), 0.10f, 0.18f),
    FishKind("刺鲀", Color(0xFFE7C780), 0.11f, 0.25f),
    FishKind("蝴蝶鱼", Color(0xFFFFD878), 0.14f, 0.24f),
    FishKind("神仙鱼", Color(0xFFB5A6D9), 0.16f, 0.27f),
    FishKind("麻雀", Color(0xFFB88C63), 0.10f, 0.16f, 1),
    FishKind("燕子", Color(0xFF5B7085), 0.18f, 0.13f, 1),
    FishKind("鹦鹉", Color(0xFF8EC572), 0.12f, 0.20f, 1),
    FishKind("兔子", Color(0xFFE8CBB7), 0.09f, 0.19f, 2),
    FishKind("狐狸", Color(0xFFD88C54), 0.20f, 0.16f, 2),
    FishKind("小鹿", Color(0xFFB99571), 0.11f, 0.20f, 2),
    FishKind("霸王龙", Color(0xFF91AD79), 0.20f, 0.21f, 3),
    FishKind("三角龙", Color(0xFF9BA889), 0.14f, 0.18f, 3),
    FishKind("剑龙", Color(0xFFB9A47C), 0.17f, 0.17f, 3),
)
private val sceneNames = listOf("鱼缸", "天空", "森林", "史前")

private fun shorter(points: List<Offset>) = points.map {
    Offset(0.5f + (it.x - 0.5f) * 0.72f, it.y)
}

private fun outline(kind: FishKind): List<Offset> {
    val creature = when (kind.name) {
        "麻雀" -> listOf(
            Offset(.19f, .54f),
            Offset(.36f, .46f),
            Offset(.47f, .31f),
            Offset(.61f, .44f),
            Offset(.77f, .40f),
            Offset(.87f, .48f),
            Offset(.94f, .51f),
            Offset(.86f, .54f),
            Offset(.74f, .62f),
            Offset(.48f, .63f),
            Offset(.35f, .57f),
            Offset(.19f, .54f)
        )

        "燕子" -> listOf(
            Offset(.33f, .54f),
            Offset(.12f, .34f),
            Offset(.22f, .55f),
            Offset(.12f, .75f),
            Offset(.36f, .59f),
            Offset(.48f, .30f),
            Offset(.59f, .48f),
            Offset(.77f, .42f),
            Offset(.90f, .50f),
            Offset(.76f, .56f),
            Offset(.57f, .60f),
            Offset(.47f, .69f),
            Offset(.33f, .54f)
        )

        "鹦鹉" -> listOf(
            Offset(.24f, .55f),
            Offset(.18f, .78f),
            Offset(.37f, .65f),
            Offset(.39f, .35f),
            Offset(.55f, .27f),
            Offset(.73f, .36f),
            Offset(.83f, .45f),
            Offset(.91f, .50f),
            Offset(.84f, .58f),
            Offset(.68f, .68f),
            Offset(.46f, .69f),
            Offset(.24f, .55f)
        )

        "兔子" -> listOf(
            Offset(.18f, .61f),
            Offset(.25f, .56f),
            Offset(.39f, .45f),
            Offset(.57f, .45f),
            Offset(.63f, .16f),
            Offset(.70f, .15f),
            Offset(.72f, .40f),
            Offset(.79f, .19f),
            Offset(.85f, .22f),
            Offset(.82f, .47f),
            Offset(.92f, .56f),
            Offset(.78f, .67f),
            Offset(.64f, .68f),
            Offset(.54f, .75f),
            Offset(.38f, .72f),
            Offset(.25f, .69f),
            Offset(.18f, .61f)
        )

        "狐狸" -> listOf(
            Offset(.29f, .54f),
            Offset(.10f, .33f),
            Offset(.13f, .68f),
            Offset(.33f, .65f),
            Offset(.47f, .44f),
            Offset(.64f, .40f),
            Offset(.72f, .28f),
            Offset(.78f, .41f),
            Offset(.87f, .46f),
            Offset(.95f, .55f),
            Offset(.81f, .61f),
            Offset(.70f, .67f),
            Offset(.52f, .69f),
            Offset(.42f, .73f),
            Offset(.29f, .54f)
        )

        "小鹿" -> listOf(
            Offset(.21f, .66f),
            Offset(.28f, .42f),
            Offset(.57f, .42f),
            Offset(.65f, .32f),
            Offset(.61f, .13f),
            Offset(.69f, .28f),
            Offset(.77f, .14f),
            Offset(.76f, .33f),
            Offset(.86f, .40f),
            Offset(.92f, .51f),
            Offset(.80f, .57f),
            Offset(.70f, .55f),
            Offset(.66f, .75f),
            Offset(.60f, .75f),
            Offset(.55f, .55f),
            Offset(.37f, .55f),
            Offset(.33f, .76f),
            Offset(.27f, .76f),
            Offset(.21f, .66f)
        )

        "霸王龙" -> listOf(
            Offset(.30f, .54f),
            Offset(.07f, .37f),
            Offset(.16f, .57f),
            Offset(.36f, .63f),
            Offset(.48f, .43f),
            Offset(.57f, .28f),
            Offset(.78f, .27f),
            Offset(.89f, .35f),
            Offset(.79f, .42f),
            Offset(.88f, .50f),
            Offset(.75f, .57f),
            Offset(.63f, .52f),
            Offset(.60f, .72f),
            Offset(.53f, .74f),
            Offset(.48f, .58f),
            Offset(.37f, .73f),
            Offset(.30f, .54f)
        )

        "三角龙" -> listOf(
            Offset(.25f, .56f),
            Offset(.09f, .44f),
            Offset(.22f, .67f),
            Offset(.37f, .65f),
            Offset(.52f, .42f),
            Offset(.69f, .36f),
            Offset(.76f, .18f),
            Offset(.81f, .37f),
            Offset(.94f, .37f),
            Offset(.84f, .49f),
            Offset(.95f, .54f),
            Offset(.80f, .61f),
            Offset(.65f, .59f),
            Offset(.60f, .75f),
            Offset(.53f, .75f),
            Offset(.48f, .62f),
            Offset(.34f, .75f),
            Offset(.25f, .56f)
        )

        "剑龙" -> listOf(
            Offset(.22f, .57f),
            Offset(.08f, .42f),
            Offset(.20f, .67f),
            Offset(.35f, .63f),
            Offset(.38f, .42f),
            Offset(.45f, .25f),
            Offset(.50f, .42f),
            Offset(.57f, .20f),
            Offset(.61f, .43f),
            Offset(.69f, .27f),
            Offset(.73f, .46f),
            Offset(.85f, .46f),
            Offset(.94f, .55f),
            Offset(.84f, .64f),
            Offset(.72f, .62f),
            Offset(.69f, .76f),
            Offset(.62f, .76f),
            Offset(.55f, .63f),
            Offset(.35f, .76f),
            Offset(.22f, .57f)
        )

        else -> null
    }
    if (creature != null) return shorter(creature)
    val special = when (kind.name) {
        "鲸鱼（哺乳动物）" -> listOf(
            Offset(.27f, .50f), Offset(.10f, .44f), Offset(.18f, .49f), Offset(.10f, .55f),
            Offset(.27f, .50f), Offset(.27f, .43f),
            Offset(.43f, .34f), Offset(.63f, .34f), Offset(.81f, .40f), Offset(.91f, .49f),
            Offset(.86f, .58f), Offset(.70f, .64f), Offset(.48f, .66f), Offset(.32f, .59f),
            Offset(.27f, .50f)
        )

        "鲨鱼" -> listOf(
            Offset(.27f, .50f), Offset(.10f, .28f), Offset(.16f, .50f), Offset(.10f, .63f),
            Offset(.27f, .52f), Offset(.43f, .40f), Offset(.53f, .22f), Offset(.59f, .39f),
            Offset(.80f, .44f), Offset(.93f, .51f), Offset(.80f, .57f), Offset(.60f, .61f),
            Offset(.47f, .69f), Offset(.43f, .59f), Offset(.27f, .50f)
        )

        "金枪鱼" -> listOf(
            Offset(.26f, .50f), Offset(.10f, .27f), Offset(.17f, .50f), Offset(.10f, .73f),
            Offset(.26f, .50f), Offset(.48f, .40f), Offset(.68f, .40f), Offset(.82f, .44f),
            Offset(.94f, .50f), Offset(.82f, .56f), Offset(.68f, .60f), Offset(.48f, .60f),
            Offset(.26f, .50f)
        )

        "长吻海马" -> listOf(
            Offset(.56f, .33f), Offset(.67f, .25f), Offset(.78f, .31f), Offset(.90f, .32f),
            Offset(.90f, .37f), Offset(.75f, .38f), Offset(.70f, .48f), Offset(.76f, .61f),
            Offset(.67f, .73f), Offset(.57f, .75f), Offset(.52f, .67f), Offset(.59f, .62f),
            Offset(.63f, .67f), Offset(.60f, .70f), Offset(.55f, .68f), Offset(.50f, .54f),
            Offset(.48f, .42f), Offset(.56f, .33f)
        )

        "刺鲀" -> listOf(
            Offset(.30f, .50f), Offset(.14f, .38f), Offset(.14f, .62f), Offset(.30f, .50f),
            Offset(.39f, .30f), Offset(.58f, .23f), Offset(.77f, .31f), Offset(.88f, .48f),
            Offset(.82f, .67f), Offset(.65f, .77f), Offset(.45f, .72f), Offset(.30f, .50f)
        )

        "蝴蝶鱼" -> listOf(
            Offset(.27f, .50f), Offset(.12f, .34f), Offset(.12f, .66f), Offset(.27f, .50f),
            Offset(.39f, .32f), Offset(.57f, .23f), Offset(.76f, .32f), Offset(.87f, .48f),
            Offset(.78f, .67f), Offset(.55f, .77f), Offset(.37f, .67f), Offset(.27f, .50f)
        )

        "神仙鱼" -> listOf(
            Offset(.27f, .50f), Offset(.12f, .30f), Offset(.12f, .70f), Offset(.27f, .50f),
            Offset(.43f, .33f), Offset(.56f, .15f), Offset(.67f, .36f), Offset(.84f, .49f),
            Offset(.68f, .65f), Offset(.56f, .85f), Offset(.42f, .67f), Offset(.27f, .50f)
        )

        else -> null
    }
    if (special != null) return shorter(special)
    val tailRoot = if (kind.name == "金鱼") 0.32f else 0.29f
    val raw = listOf(
        Offset(tailRoot, 0.5f), Offset(0.13f, 0.5f - kind.tail),
        Offset(0.13f, 0.5f + kind.tail), Offset(tailRoot, 0.5f),
        Offset(0.43f, 0.5f - kind.bodyHeight * 0.8f),
        Offset(0.61f, 0.5f - kind.bodyHeight),
        Offset(0.79f, 0.5f - kind.bodyHeight * 0.55f),
        Offset(if (kind.name == "黄倒吊") 0.91f else 0.87f, 0.5f),
        Offset(0.79f, 0.5f + kind.bodyHeight * 0.55f),
        Offset(0.61f, 0.5f + kind.bodyHeight),
        Offset(0.43f, 0.5f + kind.bodyHeight * 0.8f),
        Offset(tailRoot, 0.5f),
    )
    // 画板上的线稿只占中间约半幅，便于孩子在手机上描画。
    return shorter(raw)
}

private fun details(kind: FishKind): List<List<Offset>> = when (kind.name) {
    "公子小丑鱼" -> listOf(
        listOf(Offset(.48f, .36f), Offset(.47f, .50f), Offset(.48f, .64f)),
        listOf(Offset(.60f, .33f), Offset(.59f, .50f), Offset(.60f, .67f)),
        listOf(Offset(.74f, .37f), Offset(.73f, .50f), Offset(.74f, .63f))
    )

    "金鱼" -> listOf(
        listOf(Offset(.46f, .37f), Offset(.49f, .24f), Offset(.61f, .31f)),
        listOf(Offset(.54f, .53f), Offset(.43f, .73f), Offset(.65f, .62f))
    )

    "蓝倒吊" -> listOf(
        listOf(Offset(.36f, .48f), Offset(.52f, .37f), Offset(.70f, .44f), Offset(.74f, .56f)),
        listOf(Offset(.42f, .53f), Offset(.58f, .60f), Offset(.72f, .55f))
    )

    "黄倒吊" -> listOf(
        listOf(Offset(.42f, .38f), Offset(.54f, .27f), Offset(.69f, .34f)),
        listOf(Offset(.46f, .62f), Offset(.59f, .72f), Offset(.73f, .62f))
    )

    "鲸鱼（哺乳动物）" -> listOf(
        listOf(Offset(.48f, .65f), Offset(.53f, .78f), Offset(.65f, .64f)),
        listOf(Offset(.53f, .34f), Offset(.55f, .27f), Offset(.58f, .34f))
    )

    "鲨鱼" -> listOf(
        listOf(Offset(.60f, .61f), Offset(.65f, .75f), Offset(.73f, .59f)),
        listOf(Offset(.75f, .43f), Offset(.74f, .57f)),
        listOf(Offset(.78f, .44f), Offset(.77f, .56f))
    )

    "金枪鱼" -> listOf(
        listOf(Offset(.37f, .45f), Offset(.58f, .43f), Offset(.79f, .47f)),
        listOf(Offset(.37f, .55f), Offset(.58f, .57f), Offset(.79f, .53f)),
        listOf(Offset(.58f, .60f), Offset(.64f, .69f), Offset(.70f, .60f))
    )

    "长吻海马" -> listOf(
        listOf(Offset(.57f, .40f), Offset(.61f, .48f), Offset(.60f, .56f)),
        listOf(Offset(.54f, .34f), Offset(.51f, .29f), Offset(.57f, .28f))
    )

    "刺鲀" -> listOf(
        listOf(Offset(.43f, .35f), Offset(.38f, .25f), Offset(.50f, .27f)),
        listOf(Offset(.63f, .28f), Offset(.66f, .20f), Offset(.73f, .30f)),
        listOf(Offset(.52f, .70f), Offset(.49f, .80f), Offset(.62f, .75f))
    )

    "蝴蝶鱼" -> listOf(
        listOf(Offset(.43f, .32f), Offset(.45f, .50f), Offset(.43f, .68f)),
        listOf(Offset(.61f, .26f), Offset(.63f, .50f), Offset(.61f, .73f))
    )

    "神仙鱼" -> listOf(
        listOf(Offset(.43f, .32f), Offset(.48f, .50f), Offset(.43f, .68f)),
        listOf(Offset(.60f, .29f), Offset(.62f, .50f), Offset(.60f, .72f))
    )

    else -> when (kind.scene) {
        1 -> listOf(listOf(Offset(.43f, .54f), Offset(.55f, .46f), Offset(.69f, .52f)))
        2 -> listOf(
            listOf(Offset(.36f, .58f), Offset(.37f, .74f)),
            listOf(Offset(.59f, .57f), Offset(.61f, .74f))
        )

        3 -> listOf(
            listOf(Offset(.48f, .56f), Offset(.51f, .71f)),
            listOf(Offset(.68f, .50f), Offset(.72f, .60f))
        )

        else -> emptyList()
    }
}.map(::shorter)

private fun coverage(kind: FishKind, strokes: List<List<Offset>>): Float {
    val points = strokes.flatten()
    if (points.isEmpty()) return 0f
    val paths = listOf(outline(kind)) + details(kind)
    val samples = paths.flatMap { it.zipWithNext() }.flatMap { (a, b) ->
        (0..8).map { step ->
            Offset(
                a.x + (b.x - a.x) * step / 8f,
                a.y + (b.y - a.y) * step / 8f
            )
        }
    }
    val guideCoverage = samples.count { sample ->
        points.any { point ->
            hypot(
                (sample.x - point.x).toDouble(),
                (sample.y - point.y).toDouble()
            ) < 0.07
        }
    }.toFloat() / samples.size
    val strokePrecision = points.count { point ->
        samples.any { sample ->
            hypot((sample.x - point.x).toDouble(), (sample.y - point.y).toDouble()) < 0.055
        }
    }.toFloat() / points.size
    return guideCoverage * 0.7f + strokePrecision * 0.3f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DrawToLifeScreen(onBack: () -> Unit) {
    var scene by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf(0) }
    val strokes = remember(selected) { mutableStateListOf<List<Offset>>() }
    val fish = remember { mutableStateListOf<LivingFish>() }
    var flying by remember { mutableStateOf<LivingFish?>(null) }
    var finished by remember { mutableStateOf(false) }
    var matchPercent by remember { mutableStateOf<Int?>(null) }
    var showTankSettings by remember { mutableStateOf(false) }
    var showScenePicker by remember { mutableStateOf(false) }
    val sceneStyles = remember { mutableStateListOf(0, 0, 0, 0) }
    val largeProps = remember { mutableStateListOf(4, 3, 4, 5) }
    val smallProps = remember { mutableStateListOf(5, 3, 6, 4) }
    val flight = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val swim by rememberInfiniteTransition(label = "鱼群游动").animateFloat(
        0f, 1f, infiniteRepeatable(tween(60000, easing = LinearEasing), RepeatMode.Restart),
        label = "游动位置"
    )
    val tailSwing by rememberInfiniteTransition(label = "鱼尾摆动").animateFloat(
        -1f, 1f, infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "尾鳍角度"
    )
    val paper = Color(0xFFFFF9EA)
    val sceneColors = listOf(
        listOf(Color(0xFFB9E6E4), Color(0xFFBFDDF0), Color(0xFFD4E9DB)),
        listOf(Color(0xFFD9EEFA), Color(0xFFFFEBD5), Color(0xFFE6E6FA)),
        listOf(Color(0xFFD9E8C2), Color(0xFFFFE4C8), Color(0xFFD1E6DB)),
        listOf(Color(0xFFE9D6B4), Color(0xFFD8DDC2), Color(0xFFE8D0C7))
    )
    val tankStyle = sceneStyles[scene]
    val rockCount = largeProps[scene]
    val plantCount = smallProps[scene]
    val frameColor = listOf(
        Color(0xFF8AB9AD), Color(0xFFA9C9DA),
        Color(0xFF97B887), Color(0xFFC2A884)
    )[scene]
    val sceneCreatures = fish.filter { it.kind.scene == scene }
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF7F0DE)).safeDrawingPadding()
            .padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                "DrawToLife", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = Color(0xFF6F5949)
            )
        }
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val tankHeight = maxHeight * 0.48f
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.fillMaxWidth().height(tankHeight)
                        .clip(RoundedCornerShape(28.dp))
                        .border(4.dp, frameColor, RoundedCornerShape(28.dp))
                        .background(sceneColors[scene][tankStyle], RoundedCornerShape(28.dp))
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawRect(
                            sceneColors[scene][tankStyle], topLeft = Offset(0f, size.height * 0.15f),
                            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.85f)
                        )
                        if (scene == 0) {
                            repeat(rockCount) { i ->
                                val x = size.width * (0.11f + ((i * 37) % 78) / 100f)
                                drawCircle(
                                    Color(0xFFA5A79B), (9 + i % 3 * 3).dp.toPx(),
                                    Offset(x, size.height * 0.94f)
                                )
                            }
                            repeat(plantCount) { i ->
                                val x = size.width * (0.08f + ((i * 29) % 85) / 100f)
                                val bottom = size.height * 0.98f
                                val top = size.height * (0.70f - i % 3 * 0.05f)
                                drawLine(
                                    Color(0xFF6FAF8B), Offset(x, bottom),
                                    Offset(x + (i % 2 * 2 - 1) * 8.dp.toPx(), top), 4.dp.toPx()
                                )
                                drawCircle(Color(0xFF80BF98), 5.dp.toPx(), Offset(x, top))
                            }
                            repeat(9) { i ->
                                drawCircle(
                                    Color.White.copy(alpha = 0.32f), (3 + i % 3).dp.toPx(),
                                    Offset(
                                        size.width * ((i * 23 % 89) + 5) / 100f,
                                        size.height * ((i * 19 % 65) + 22) / 100f
                                    )
                                )
                            }
                        } else drawSceneProps(scene, rockCount, plantCount)
                        sceneCreatures.forEachIndexed { index, item ->
                            val phase = swim * 6.283185f
                            val seed = index * 1.73f + 0.8f
                            val mode = when (item.kind.name) {
                                "金枪鱼" -> 3 // 短距离快游
                                "长吻海马" -> 1 // 主要上下活动
                                "刺鲀" -> 2 // 近乎停驻
                                else -> when (scene) {
                                    1 -> if (index % 3 == 0) 1 else 0
                                    2, 3 -> when (index % 3) {
                                        0 -> 0; 1 -> 3; else -> 2
                                    }

                                    else -> index % 4
                                }
                            }
                            val baseX = 0.22f + ((index * 17) % 55) / 100f
                            val baseY = if (scene >= 2) 0.73f
                            else 0.34f + ((index * 13) % 35) / 100f
                            val horizontal = when (mode) {
                                0 -> 0.5f + 0.32f * sin(phase * 2f + seed)
                                1 -> baseX + 0.025f * sin(phase * 2f + seed)
                                2 -> baseX + 0.008f * sin(phase + seed)
                                else -> 0.5f + 0.30f * sin(phase * 8f + seed)
                            }
                            val vertical = when (mode) {
                                0 -> baseY + (if (scene >= 2) 0.005f else 0.025f) * sin(phase * 3f + seed)
                                1 -> 0.5f + 0.20f * sin(phase * 2f + seed * 1.4f)
                                2 -> baseY + 0.009f * sin(phase + seed * 1.4f)
                                else -> baseY + 0.05f * sin(phase * 4f + seed)
                            }
                            val direction = when (mode) {
                                0 -> cos(phase * 2f + seed)
                                3 -> cos(phase * 8f + seed)
                                else -> if (index % 2 == 0) 1f else -1f
                            }
                            val diameter = 78.dp.toPx()
                            val groundY = size.height * 0.83f
                            val jump = if (scene >= 2 && index % 3 == 1)
                                maxOf(0f, sin(phase * 4f + seed)) * 0.12f * size.height else 0f
                            val centerY = if (scene >= 2)
                                groundY - (outline(item.kind).maxOf { it.y } - 0.5f) * diameter - jump
                            else size.height * vertical
                            val center = Offset(size.width * horizontal, centerY)
                            if (scene >= 2) drawOval(
                                Color(0xFF665A49).copy(alpha = 0.16f - jump / size.height * 0.5f),
                                topLeft = Offset(center.x - 22.dp.toPx(), groundY - 3.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(44.dp.toPx(), 6.dp.toPx())
                            )
                            val tailAmount = when (mode) {
                                2 -> 0.12f; 3 -> 1f; else -> 0.45f
                            }
                            drawFish(item, center, diameter, direction, tailSwing * tailAmount)
                        }
                    }
                    Row(
                        Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${sceneNames[scene]} · ${sceneCreatures.size} 只", Modifier.padding(start = 8.dp),
                            color = Color(0xFF426F70), style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showScenePicker = true }) {
                                Icon(
                                    Icons.Filled.ChangeCircle, contentDescription = "Change",
                                    tint = Color(0xFF426F70)
                                )
                            }
                            IconButton(onClick = { showTankSettings = true }) {
                                Icon(
                                    Icons.Filled.Edit, contentDescription = "编辑场景",
                                    tint = Color(0xFF426F70)
                                )
                            }
                        }
                    }
                    if (sceneCreatures.isNotEmpty()) Text(
                        sceneCreatures.takeLast(4).joinToString(" · ") { it.kind.name },
                        Modifier.align(Alignment.BottomCenter).padding(12.dp),
                        color = Color(0xFF426F70), style = MaterialTheme.typography.labelMedium
                    )
                }
                Column(
                    Modifier.fillMaxWidth().weight(1f)
                        .background(paper, RoundedCornerShape(28.dp))
                        .border(2.dp, Color(0xFFD8C5A8), RoundedCornerShape(28.dp))
                        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (finished) "画得真棒！这是${kinds[selected].name} · 匹配度${matchPercent ?: 0}%"
                            else "照着线条画一画",
                            color = Color(0xFF785F4D), fontWeight = FontWeight.Bold
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { strokes.clear(); finished = false; matchPercent = null }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Text("刷新")
                        }
                        Button(onClick = {
                            strokes.clear()
                            finished = false
                            matchPercent = null
                            val choices = kinds.indices.filter { kinds[it].scene == scene }
                            selected = choices[(choices.indexOf(selected) + 1) % choices.size]
                        }, modifier = Modifier.weight(1f)) {
                            Text("下一个")
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                    Canvas(
                        Modifier.fillMaxWidth().weight(1f)
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .pointerInput(selected, finished) {
                                detectDragGestures(onDragStart = { start ->
                                    if (!finished) strokes.add(listOf(Offset(start.x / size.width, start.y / size.height)))
                                }, onDragEnd = {
                                    val match = coverage(kinds[selected], strokes)
                                    if (!finished && match >= 0.55f && flying == null) {
                                        finished = true
                                        matchPercent = (match * 100f).roundToInt().coerceIn(0, 100)
                                        val made = LivingFish(kinds[selected], strokes.map { it.toList() })
                                        flying = made
                                        scope.launch {
                                            flight.snapTo(0f)
                                            flight.animateTo(1f, tween(900))
                                            fish.add(made)
                                            flying = null
                                        }
                                    }
                                }) { change, _ ->
                                    if (!finished && strokes.isNotEmpty()) {
                                        strokes[strokes.lastIndex] = strokes.last() + Offset(
                                            change.position.x / size.width, change.position.y / size.height
                                        )
                                    }
                                    change.consume()
                                }
                            }) {
                        val guidePaths = listOf(outline(kinds[selected])) + details(kinds[selected])
                        guidePaths.forEach { points ->
                            val path = Path().apply {
                                points.forEachIndexed { index, p ->
                                    if (index == 0) moveTo(p.x * size.width, p.y * size.height)
                                    else lineTo(p.x * size.width, p.y * size.height)
                                }
                            }
                            drawPath(
                                path = path, color = Color(0xFFC9BBAA),
                                style = Stroke(
                                    width = 1.6.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(3.dp.toPx(), 2.dp.toPx())
                                    )
                                )
                            )
                        }
                        drawCircle(
                            Color(0xFF9C8C7E), 2.5.dp.toPx(),
                            Offset(size.width * 0.71f, size.height * 0.45f)
                        )
                        strokes.forEach { stroke ->
                            stroke.zipWithNext().forEach { (a, b) ->
                                drawLine(
                                    kinds[selected].color, Offset(a.x * size.width, a.y * size.height),
                                    Offset(b.x * size.width, b.y * size.height), 6.dp.toPx()
                                )
                            }
                        }
                    }
                    Text(
                        "沿浅色线条描画；完成后作品会进入${sceneNames[scene]}。",
                        style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B7664)
                    )
                }
            }
            flying?.let { item ->
                Canvas(Modifier.fillMaxSize()) {
                    drawFish(
                        item, Offset(
                            size.width * 0.5f,
                            size.height * (0.72f - 0.42f * flight.value)
                        ),
                        90.dp.toPx(), 1f, tailSwing
                    )
                }
            }
        }
    }
    if (showScenePicker) ModalBottomSheet(onDismissRequest = { showScenePicker = false }) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("切换场景", style = MaterialTheme.typography.titleLarge)
            sceneNames.indices.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { index ->
                        Column(
                            Modifier.weight(1f)
                            .border(
                                2.dp, if (scene == index) Color(0xFF6E9F8B)
                                else Color(0xFFD8C5A8), RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                scene = index
                                selected = kinds.indexOfFirst { it.scene == index }
                                strokes.clear()
                                finished = false
                                matchPercent = null
                                showScenePicker = false
                            }.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Canvas(
                                Modifier.fillMaxWidth().height(88.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(sceneColors[index][sceneStyles[index]])
                            ) {
                                if (index == 0) {
                                    drawCircle(
                                        Color(0xFF9FAAA1), 9.dp.toPx(),
                                        Offset(size.width * 0.2f, size.height * 0.88f)
                                    )
                                    drawLine(
                                        Color(0xFF73AD8F),
                                        Offset(size.width * 0.77f, size.height),
                                        Offset(size.width * 0.77f, size.height * 0.55f), 4.dp.toPx()
                                    )
                                } else drawSceneProps(
                                    index, largeProps[index].coerceAtMost(3),
                                    smallProps[index].coerceAtMost(3)
                                )
                                val sample = kinds.first { it.scene == index }
                                drawFish(
                                    LivingFish(sample, emptyList()),
                                    Offset(size.width * 0.55f, size.height * 0.48f),
                                    minOf(size.width * 0.6f, size.height * 0.85f), 1f, 0f
                                )
                            }
                            Text(
                                if (scene == index) "✓ ${sceneNames[index]}" else sceneNames[index],
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
    }
    if (showTankSettings) ModalBottomSheet(onDismissRequest = { showTankSettings = false }) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("装扮${sceneNames[scene]}", style = MaterialTheme.typography.titleLarge)
            Text("场景颜色", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                sceneColors[scene].forEachIndexed { index, color ->
                    Button(
                        onClick = { sceneStyles[scene] = index }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color, contentColor = Color(0xFF34575A)
                        )
                    ) {
                        Text(
                            if (tankStyle == index) "✓ 样式${index + 1}" else "样式${index + 1}"
                        )
                    }
                }
            }
            Text("${listOf("石头", "云朵", "树木", "岩石")[scene]} · $rockCount")
            Slider(
                value = rockCount.toFloat(), onValueChange = { largeProps[scene] = it.roundToInt() },
                valueRange = 0f..10f, steps = 9
            )
            Text("${listOf("水草", "树枝", "小草", "蕨类")[scene]} · $plantCount")
            Slider(
                value = plantCount.toFloat(), onValueChange = { smallProps[scene] = it.roundToInt() },
                valueRange = 0f..10f, steps = 9
            )
            Button(onClick = { showTankSettings = false }, modifier = Modifier.fillMaxWidth()) {
                Text("完成")
            }
        }
    }
}

private fun DrawScope.drawFish(
    fish: LivingFish, center: Offset, diameter: Float, direction: Float, tailSwing: Float
) {
    val left = center.x - diameter / 2f
    val top = center.y - diameter / 2f
    fun point(p: Offset): Offset {
        val movement = when {
            fish.kind.scene == 1 && p.x in 0.40f..0.70f && p.y < 0.50f ->
                tailSwing * (0.50f - p.y) * 0.22f // 鸟翼
            fish.kind.scene >= 2 && p.x in 0.27f..0.70f && p.y > 0.65f ->
                tailSwing * (p.y - 0.65f) * 0.09f // 腿部
            p.x < 0.38f -> tailSwing * (0.38f - p.x) * 0.24f // 尾鳍或尾巴
            else -> 0f
        }
        return Offset(
            left + (0.5f + (p.x - 0.5f) * direction) * diameter,
            top + (p.y + movement) * diameter
        )
    }

    val guide = outline(fish.kind)
    val body = Path().apply {
        val first = point(guide.first())
        moveTo(first.x, first.y)
        guide.drop(1).forEach { val p = point(it); lineTo(p.x, p.y) }
        close()
    }
    drawPath(body, fish.kind.color.copy(alpha = 0.85f))
    drawPath(body, Color(0xFF6E6656), style = Stroke(2.dp.toPx()))
    details(fish.kind).forEach { line ->
        line.zipWithNext().forEach { (a, b) ->
            drawLine(Color(0xFF5D6570).copy(alpha = 0.75f), point(a), point(b), 1.5.dp.toPx())
        }
    }
    when (fish.kind.name) {
        "公子小丑鱼" -> listOf(0.46f, 0.57f, 0.68f).forEach { x ->
            drawLine(
                Color(0xFF4D4B4A), point(Offset(x, 0.38f)),
                point(Offset(x, 0.62f)), 6.dp.toPx()
            )
            drawLine(
                Color.White, point(Offset(x, 0.38f)),
                point(Offset(x, 0.62f)), 4.dp.toPx()
            )
        }

        "蓝倒吊" -> {
            drawLine(
                Color(0xFF203C74), point(Offset(0.43f, 0.41f)),
                point(Offset(0.73f, 0.56f)), 5.dp.toPx()
            )
            drawLine(
                Color(0xFFF8D658), point(Offset(0.22f, 0.40f)),
                point(Offset(0.22f, 0.60f)), 7.dp.toPx()
            )
        }

        else -> Unit
    }
    fish.strokes.forEach { stroke ->
        stroke.zipWithNext().forEach { (a, b) ->
            drawLine(Color(0xFF6E6656).copy(alpha = 0.55f), point(a), point(b), 1.5.dp.toPx())
        }
    }
    drawCircle(Color(0xFF473E38), 2.dp.toPx(), point(Offset(0.71f, 0.45f)))
}

private fun DrawScope.drawSceneProps(scene: Int, largeCount: Int, smallCount: Int) {
    val ground = size.height * 0.83f
    if (scene == 1) {
        repeat(largeCount) { i ->
            val x = size.width * (0.12f + ((i * 31) % 76) / 100f)
            val y = size.height * (0.18f + i % 3 * 0.14f)
            drawCircle(Color.White.copy(alpha = 0.78f), 15.dp.toPx(), Offset(x, y))
            drawCircle(Color.White.copy(alpha = 0.78f), 11.dp.toPx(), Offset(x + 14.dp.toPx(), y))
        }
        repeat(smallCount) { i ->
            val y = size.height * (0.72f + i % 2 * 0.10f)
            drawLine(
                Color(0xFF8D745D), Offset(0f, y),
                Offset(size.width * (0.28f + i % 3 * 0.16f), y - 7.dp.toPx()), 6.dp.toPx()
            )
            drawCircle(
                Color(0xFF93BE83), 7.dp.toPx(),
                Offset(size.width * (0.18f + i % 3 * 0.15f), y - 10.dp.toPx())
            )
        }
    } else {
        drawRect(
            if (scene == 2) Color(0xFFB8D59E) else Color(0xFFD7BC91),
            topLeft = Offset(0f, ground),
            size = androidx.compose.ui.geometry.Size(size.width, size.height - ground)
        )
        repeat(largeCount) { i ->
            val x = size.width * (0.10f + ((i * 29) % 80) / 100f)
            if (scene == 2) {
                drawLine(
                    Color(0xFF8D745D), Offset(x, ground),
                    Offset(x, ground - 36.dp.toPx()), 9.dp.toPx()
                )
                drawCircle(
                    Color(0xFF84AD71), 24.dp.toPx(),
                    Offset(x, ground - 45.dp.toPx())
                )
            } else {
                drawCircle(
                    Color(0xFFAA987F), (9 + i % 3 * 4).dp.toPx(),
                    Offset(x, ground)
                )
            }
        }
        repeat(smallCount) { i ->
            val x = size.width * (0.08f + ((i * 23) % 84) / 100f)
            val height = if (scene == 2) 13.dp.toPx() else 22.dp.toPx()
            drawLine(
                Color(0xFF73A573), Offset(x, ground),
                Offset(x - 4.dp.toPx(), ground - height), 3.dp.toPx()
            )
            drawLine(
                Color(0xFF73A573), Offset(x, ground),
                Offset(x + 5.dp.toPx(), ground - height * 0.8f), 3.dp.toPx()
            )
        }
    }
}
