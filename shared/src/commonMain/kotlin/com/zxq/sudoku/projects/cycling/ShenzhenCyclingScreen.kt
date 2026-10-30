package com.zxq.sudoku.projects.cycling

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private data class CyclingRoute(
    val name: String,
    val district: String,
    val distance: String,
    val difficulty: String,
    val duration: String,
    val start: String,
    val end: String,
    val via: String,
    val description: String,
    val sourceUrl: String,
    val points: List<Pair<Float, Float>>,
)

private const val citySource = "https://zjj.sz.gov.cn/xxgk/gzdt/content/post_12942679.html"

// 里程和地名取自资料；难度、耗时为休闲骑行估算，折线仅为方向示意。
private val routes = listOf(
    CyclingRoute(
        "盐田海滨休闲线", "盐田", "约 4 公里", "入门", "约 30–50 分钟",
        "盐田墟地铁站", "大梅沙海滨公园", "海滨绿道 · 盐田海鲜街",
        "沿着盐田港海岸轻松骑到大梅沙，基本平路，适合第一次体验海边骑行。高峰时留意行人，晴天记得防晒。",
        "https://www.sznews.com/news/content/2026-10/13/content_32190943.htm",
        listOf(.13f to .70f, .25f to .57f, .38f to .64f, .51f to .43f, .66f to .48f, .82f to .27f),
    ),
    CyclingRoute(
        "大沙河生态长廊", "南山", "13.7 公里", "轻松", "约 2–4 小时",
        "长岭陂水库", "深圳湾入海口", "大学城 · 大沙河中游",
        "沿水岸从大学城骑向深圳湾，绿道平整，设有独立并行的步行道与自行车道。适合边骑边停，欣赏河岸与白鹭。",
        citySource,
        listOf(.40f to .16f, .54f to .28f, .46f to .41f, .60f to .55f, .53f to .70f, .68f to .84f),
    ),
    CyclingRoute(
        "前海沿海骑行绿道", "南山 · 前海", "约 4 公里", "入门", "约 30–60 分钟",
        "前海石公园", "欢乐港湾", "前海演艺公园 · 双界河公园 · 桂湾公园",
        "串联前海的公园与观海空间，适合短途慢骑。可以在草坪和观景处休息，傍晚骑行尤其惬意。",
        citySource,
        listOf(.15f to .62f, .31f to .52f, .42f to .63f, .57f to .42f, .68f to .50f, .84f to .30f),
    ),
    CyclingRoute(
        "梧桐绿道", "罗湖", "约 14.9 公里", "中等", "约 2–3 小时",
        "东湖公园万佛寺驿站", "横沥口水库", "深圳水库 · 荔枝林 · 梧桐河",
        "路线穿过水库、果林和河滨三段不同风景。里程较长，建议带水并为途中休息留出时间。",
        citySource,
        listOf(.16f to .69f, .30f to .46f, .44f to .53f, .55f to .29f, .70f to .38f, .82f to .19f),
    ),
    CyclingRoute(
        "环西丽湖绿道", "南山", "16.2 公里", "中等", "约 2–3 小时",
        "西丽湖环湖主线", "返回环湖起点", "一环九景 · 湖畔骑行驿站",
        "围湖骑行，沿途可看到山林、田溪和湖景。骑行道与步行道分离，适合安排一个从容的半日行程。",
        citySource,
        listOf(.29f to .30f, .53f to .17f, .77f to .34f, .80f to .64f, .57f to .79f, .31f to .69f, .20f to .47f, .40f to .39f),
    ),
)

@Composable
internal fun ShenzhenCyclingScreen(onBack: () -> Unit) {
    var selectedIndex by remember { mutableStateOf(0) }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回项目首页")
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text("深圳骑行路线", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold)
                Text("选一条路线，去看看城市的另一面", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= 760.dp) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.width(286.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("路线 · ${routes.size} 条", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        routes.forEachIndexed { index, route ->
                            RouteItem(route, index == selectedIndex, { selectedIndex = index })
                        }
                    }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        RouteDetail(routes[selectedIndex], 280.dp,
                            Modifier.widthIn(max = 840.dp).fillMaxWidth())
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("路线 · ${routes.size} 条", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        routes.forEachIndexed { index, route ->
                            RouteItem(route, index == selectedIndex, { selectedIndex = index },
                                Modifier.width(210.dp))
                        }
                    }
                    RouteDetail(routes[selectedIndex], 210.dp, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun RouteItem(
    route: CyclingRoute,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = if (selected)
            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(route.district, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(route.name, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text("${route.distance}  ·  ${route.difficulty}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RouteDetail(route: CyclingRoute, mapHeight: Dp, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(route.name, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("${route.district} · 路线示意图", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RouteMap(route, mapHeight)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("距离", route.distance, Modifier.weight(1f))
            StatCard("难度", route.difficulty, Modifier.weight(1f))
            StatCard("预计耗时", route.duration, Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("沿途一览", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text("起点  ${route.start}", style = MaterialTheme.typography.bodyMedium)
                Text("途经  ${route.via}", style = MaterialTheme.typography.bodyMedium)
                Text("终点  ${route.end}", style = MaterialTheme.typography.bodyMedium)
                Text(route.description, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("耗时与难度为估算；地图仅示意，不用于导航。",
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = { uriHandler.openUri(route.sourceUrl) }) { Text("资料来源") }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RouteMap(route: CyclingRoute, height: Dp) {
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(24.dp))
        .background(colors.secondaryContainer)) {
        Canvas(Modifier.fillMaxSize()) {
            val gridColor = colors.onSecondaryContainer.copy(alpha = .08f)
            for (step in 1..6) {
                val x = size.width * step / 7f
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
            }
            for (step in 1..4) {
                val y = size.height * step / 5f
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            val path = Path().apply {
                route.points.forEachIndexed { index, point ->
                    val x = point.first * size.width
                    val y = point.second * size.height
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(path, colors.surface, style = Stroke(width = 13.dp.toPx(),
                cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, colors.primary, style = Stroke(width = 6.dp.toPx(),
                cap = StrokeCap.Round, join = StrokeJoin.Round))
            listOf(route.points.first(), route.points.last()).forEachIndexed { index, point ->
                val center = Offset(point.first * size.width, point.second * size.height)
                drawCircle(colors.surface, radius = 17.dp.toPx(), center = center)
                drawCircle(if (index == 0) colors.tertiary else colors.primary,
                    radius = 14.dp.toPx(), center = center)
            }
        }
        val first = route.points.first()
        val last = route.points.last()
        Text("起", Modifier.offset(x = maxWidth * first.first - 8.dp,
            y = maxHeight * first.second - 11.dp),
            style = MaterialTheme.typography.labelMedium, color = colors.onTertiary)
        Text("终", Modifier.offset(x = maxWidth * last.first - 8.dp,
            y = maxHeight * last.second - 11.dp),
            style = MaterialTheme.typography.labelMedium, color = colors.onPrimary)
    }
}
