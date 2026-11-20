# 山野同行 · 云南 2027 静态网站

## 文件
- `index.html`：页面结构和 SEO
- `styles.css`：响应式视觉设计
- `script.js`：行程筛选、预算切换、Leaflet 地图和分享功能

## 本地预览
直接双击 `index.html`，或在此目录运行 `python -m http.server 8000` 并访问 http://localhost:8000 。地图、字体与远程图片需要联网。

## 部署 Cloudflare Pages
1. 登录 Cloudflare Dashboard → Workers & Pages → Create → Pages → Upload assets。
2. 上传本目录的文件（或上传整个目录），确保 `index.html` 位于站点根目录。
3. 部署完成后获得 `*.pages.dev` 链接，分享按钮将自动分享当前网址。

## 自定义
- 日期、每日安排：编辑 `script.js` 中的 `days`。
- 三档预算：编辑 `script.js` 中的 `tiers`。
- 图片：编辑 `styles.css` 中 `.hero-photo`、`.p1`、`.p2`、`.p3`、`.quote` 的背景图片 URL。当前是通用山景氛围图，**不代表具体目的地实拍**。如需严格匹配地点，请替换为自己拍摄或有授权的照片。
- 地图使用 OpenStreetMap 公共瓦片和 Leaflet CDN，依赖联网。地图标记为大致位置，虚线是目的地之间的示意连线，**不是公路导航或徒步轨迹**。

## 安全说明
此页面是计划提案，不代表景区已批准所有活动。2027年2月为冬季，高海拔积雪、道路封闭和高反风险不容忽视。冰湖、神湖、尼色等未开放线路默认排除。出发前逐项核对官方最新通告，必要时取消雨崩段。预算是粗略估算，春节价格可能大幅高于预估。
