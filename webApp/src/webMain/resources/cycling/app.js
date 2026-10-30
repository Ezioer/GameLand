const citySource = 'https://zjj.sz.gov.cn/xxgk/gzdt/content/post_12942679.html';

// 地名坐标只用于定位地图标记。没有 GPX 前不绘制骑行轨迹。
const routes = [
    {
        name: '盐田海滨休闲线', district: '盐田', distance: '约 4 公里',
        difficulty: '入门', duration: '约 30–50 分钟',
        start: '盐田墟地铁站', end: '大梅沙海滨公园', via: '海滨绿道 · 盐田海鲜街',
        description: '沿着盐田港海岸轻松骑到大梅沙，基本平路，适合第一次体验海边骑行。高峰时留意行人，晴天记得防晒。',
        sourceUrl: 'https://www.sznews.com/news/content/2026-10/13/content_32190943.htm',
        startPoint: [114.2695698, 22.5906371], endPoint: [114.3020948, 22.5943462],
    },
    {
        name: '大沙河生态长廊', district: '南山', distance: '13.7 公里',
        difficulty: '轻松', duration: '约 2–4 小时',
        start: '长岭陂水库', end: '深圳湾入海口', via: '大学城 · 大沙河中游',
        description: '沿水岸从大学城骑向深圳湾，绿道平整，设有独立并行的步行道与自行车道。适合边骑边停，欣赏河岸与白鹭。',
        sourceUrl: citySource,
        startPoint: [114.0009192, 22.6143289], endPoint: [113.953, 22.500],
    },
    {
        name: '前海沿海骑行绿道', district: '南山 · 前海', distance: '约 4 公里',
        difficulty: '入门', duration: '约 30–60 分钟',
        start: '前海石公园', end: '欢乐港湾', via: '前海演艺公园 · 双界河公园 · 桂湾公园',
        description: '串联前海的公园与观海空间，适合短途慢骑。可以在草坪和观景处休息，傍晚骑行尤其惬意。',
        sourceUrl: citySource,
        startPoint: [113.8847574, 22.5291392], endPoint: [113.8810110, 22.5463106],
    },
    {
        name: '梧桐绿道', district: '罗湖', distance: '约 14.9 公里',
        difficulty: '中等', duration: '约 2–3 小时',
        start: '东湖公园万佛寺驿站', end: '横沥口水库', via: '深圳水库 · 荔枝林 · 梧桐河',
        description: '路线穿过水库、果林和河滨三段不同风景。里程较长，建议带水并为途中休息留出时间。',
        sourceUrl: citySource,
        startPoint: [114.1427042, 22.5585535], endPoint: [114.1971569, 22.5925752],
    },
    {
        name: '环西丽湖绿道', district: '南山', distance: '16.2 公里',
        difficulty: '中等', duration: '约 2–3 小时',
        start: '白芒拓印入口', end: '返回白芒拓印入口', via: '一环九景 · 湖畔骑行驿站',
        description: '围湖骑行，沿途可看到山林、田溪和湖景。骑行道与步行道分离，适合安排一个从容的半日行程。',
        sourceUrl: citySource,
        startPoint: [113.9373361, 22.6262453], endPoint: null,
        viewPoint: [113.9605400, 22.5964998],
    },
];

let selectedIndex = 0;
let map;
let maplibre;
let mapReady = false;
let markers = [];

function setText(id, value) {
    document.getElementById(id).textContent = value;
}

function renderList() {
    const list = document.getElementById('route-list');
    list.replaceChildren();
    routes.forEach((route, index) => {
        const button = document.createElement('button');
        button.className = 'route-item';
        button.type = 'button';
        button.setAttribute('aria-pressed', String(index === selectedIndex));
        for (const [className, value] of [
            ['district', route.district],
            ['name', route.name],
            ['summary', `${route.distance}  ·  ${route.difficulty}`],
        ]) {
            const span = document.createElement('span');
            span.className = className;
            span.textContent = value;
            button.append(span);
        }
        button.addEventListener('click', () => {
            selectedIndex = index;
            renderList();
            renderDetail();
            updateMap();
        });
        list.append(button);
    });
}

function renderDetail() {
    const route = routes[selectedIndex];
    for (const key of ['distance', 'difficulty', 'duration', 'district', 'start', 'via', 'end', 'description']) {
        setText(key, route[key]);
    }
    setText('route-name', route.name);
    document.getElementById('source-link').href = route.sourceUrl;
    document.getElementById('map').setAttribute('aria-label', `${route.name}附近的地图`);
}

function addMarker(point, label, kind) {
    const element = document.createElement('div');
    element.className = `map-pin ${kind}`;
    element.title = label;
    markers.push(new maplibre.Marker({ element })
        .setLngLat(point)
        .setPopup(new maplibre.Popup({ offset: 16 }).setText(label))
        .addTo(map));
}

function updateMap() {
    if (!mapReady) return;
    const route = routes[selectedIndex];
    markers.forEach(marker => marker.remove());
    markers = [];
    addMarker(route.startPoint, `起点附近：${route.start}`, 'start');
    if (route.endPoint) addMarker(route.endPoint, `终点附近：${route.end}`, 'end');

    const bounds = new maplibre.LngLatBounds(route.startPoint, route.startPoint);
    if (route.endPoint) bounds.extend(route.endPoint);
    if (route.viewPoint) bounds.extend(route.viewPoint);
    map.fitBounds(bounds, { padding: 48, maxZoom: 13.5, duration: 650 });
}

async function loadMap() {
    try {
        maplibre = await import('https://unpkg.com/maplibre-gl@6.15.0/dist/maplibre-gl.mjs');
        map = new maplibre.Map({
            container: 'map',
            style: 'https://tiles.openfreemap.org/styles/liberty',
            center: routes[0].startPoint,
            zoom: 11,
        });
        map.addControl(new maplibre.NavigationControl({ showCompass: false }), 'top-right');
        map.once('load', () => {
            mapReady = true;
            updateMap();
        });
    } catch (error) {
        console.error('骑行地图加载失败', error);
        document.getElementById('map-error').hidden = false;
    }
}

renderList();
renderDetail();
loadMap();
