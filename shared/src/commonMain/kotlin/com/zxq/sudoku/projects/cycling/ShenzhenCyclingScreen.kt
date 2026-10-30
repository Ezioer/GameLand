package com.zxq.sudoku.projects.cycling

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sudoku.shared.generated.resources.Res
import sudoku.shared.generated.resources.cycling_dashahe
import sudoku.shared.generated.resources.cycling_qianhai
import sudoku.shared.generated.resources.cycling_wutong
import sudoku.shared.generated.resources.cycling_xili
import sudoku.shared.generated.resources.cycling_yantian

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
    val map: DrawableResource,
    val mapNote: String,
)

private const val citySource = "https://zjj.sz.gov.cn/xxgk/gzdt/content/post_12942679.html"

// 里程和地名取自资料；难度、耗时为休闲骑行估算，地图虚线仅为方向示意。
private val routes = listOf(
    CyclingRoute(
        "盐田海滨休闲线", "盐田", "约 4 公里", "入门", "约 30–50 分钟",
        "盐田墟地铁站", "大梅沙海滨公园", "海滨绿道 · 盐田海鲜街",
        "沿着盐田港海岸轻松骑到大梅沙，基本平路，适合第一次体验海边骑行。高峰时留意行人，晴天记得防晒。",
        "https://www.sznews.com/news/content/2026-10/13/content_32190943.htm",
        Res.drawable.cycling_yantian, "实心图钉为起点，空心图钉为终点；虚线仅表示大致方向。",
    ),
    CyclingRoute(
        "大沙河生态长廊", "南山", "13.7 公里", "轻松", "约 2–4 小时",
        "长岭陂水库", "深圳湾入海口", "大学城 · 大沙河中游",
        "沿水岸从大学城骑向深圳湾，绿道平整，设有独立并行的步行道与自行车道。适合边骑边停，欣赏河岸与白鹭。",
        citySource,
        Res.drawable.cycling_dashahe, "实心图钉为起点，空心图钉为终点；虚线仅表示大致方向。",
    ),
    CyclingRoute(
        "前海沿海骑行绿道", "南山 · 前海", "约 4 公里", "入门", "约 30–60 分钟",
        "前海石公园", "欢乐港湾", "前海演艺公园 · 双界河公园 · 桂湾公园",
        "串联前海的公园与观海空间，适合短途慢骑。可以在草坪和观景处休息，傍晚骑行尤其惬意。",
        citySource,
        Res.drawable.cycling_qianhai, "实心图钉为起点，空心图钉为终点；虚线仅表示大致方向。",
    ),
    CyclingRoute(
        "梧桐绿道", "罗湖", "约 14.9 公里", "中等", "约 2–3 小时",
        "东湖公园万佛寺驿站", "横沥口水库", "深圳水库 · 荔枝林 · 梧桐河",
        "路线穿过水库、果林和河滨三段不同风景。里程较长，建议带水并为途中休息留出时间。",
        citySource,
        Res.drawable.cycling_wutong, "实心图钉为起点，空心图钉为终点；虚线仅表示大致方向。",
    ),
    CyclingRoute(
        "环西丽湖绿道", "南山", "16.2 公里", "中等", "约 2–3 小时",
        "白芒拓印入口", "返回白芒拓印入口", "一环九景 · 湖畔骑行驿站",
        "围湖骑行，沿途可看到山林、田溪和湖景。骑行道与步行道分离，适合安排一个从容的半日行程。",
        citySource,
        Res.drawable.cycling_xili, "图钉标注白芒村附近，具体入口以现场指引为准。",
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
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= 760.dp) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.width(286.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CyclingHeader(onBack)
                        Text("路线 · ${routes.size} 条", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        routes.forEachIndexed { index, route ->
                            RouteItem(route, index == selectedIndex, { selectedIndex = index })
                        }
                    }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        RouteDetail(routes[selectedIndex],
                            Modifier.widthIn(max = 840.dp).fillMaxWidth())
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CyclingHeader(onBack)
                    Text("路线 · ${routes.size} 条", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        routes.forEachIndexed { index, route ->
                            RouteItem(route, index == selectedIndex, { selectedIndex = index },
                                Modifier.width(210.dp))
                        }
                    }
                    RouteDetail(routes[selectedIndex], Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun CyclingHeader(onBack: () -> Unit) {
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
private fun RouteDetail(route: CyclingRoute, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Image(
            painter = painterResource(route.map),
            contentDescription = "${route.name}地图，${route.mapNote}",
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(route.mapNote, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text("耗时与难度为估算；地图仅供位置参考，不用于导航。",
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = { uriHandler.openUri(route.sourceUrl) }) { Text("资料来源") }
        }
        Text("地图数据 © OpenStreetMap contributors · 底图 OpenFreeMap",
            modifier = Modifier.clickable { uriHandler.openUri("https://www.openstreetmap.org/copyright") },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary)
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
