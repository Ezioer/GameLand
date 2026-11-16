# 项目开发指南

## 协作约定

- 默认用简洁中文沟通。动手前说明准备修改的文件和计划；执行命令前说明用途。
- 改动保持小而集中，方便审查。先检查 `git status`，保留用户已有的未提交改动。
- 只写经过仓库核实的路径、配置和行为；不要在日志、文档或回复中泄露密钥、Token、账号凭据等敏感信息。
- 行为变化尽量在 `shared/src/commonTest` 添加针对规则或状态转换的测试。用户此前要求不要本机编译；除非用户明确改变要求，不运行 Gradle 编译或构建。可做静态检查，并如实说明未运行的验证。

## 项目结构与跨平台边界

- 这是 Kotlin Multiplatform + Compose Multiplatform 项目。`settings.gradle.kts` 包含 `:shared`、`:androidApp`、`:desktopApp`、`:webApp`；另有 `iosApp` 入口。
- 优先把可共享的 UI、游戏规则、状态、统计和导航放到 `shared/src/commonMain/kotlin/com/zxq/sudoku/`。不要因为先做 Android 就把通用逻辑移进 `androidApp`。
- 功能按目录组织：小游戏在 `games/<game>/`，合集页面在 `collection/`，独立项目在 `projects/<project>/`，全局导航在 `navigation/`，外观在 `theme/`。新增游戏沿用同目录内的 `*Game.kt`、`*Session.kt`、`*Screens.kt`、`*Navigation.kt`、统计代码的分工，按实际需要增减文件。
- 访问相册、音频、平台信息等能力时，在 `commonMain` 声明接口或 `expect`，在 `androidMain`、`iosMain`、`jvmMain`、`jsMain`、`wasmJsMain` 实现相应平台行为。已有例子是 `projects/albums/AlbumMedia.kt`、`AlbumMusic.kt` 和 `Platform.kt`。平台入口也可向 `App` 注入持久化、时间、系统外观和通信能力。
- 共享资源放 `shared/src/commonMain/composeResources`。现有字体位于 `font/`，骑行图片位于 `drawable/`；共享 UI 不引用 Android `R` 资源。

## UI 与导航

- 延续现有 Material 3 Expressive、柔和的“游戏小岛”视觉语言。全局颜色和字体从 `IslandTheme`、`AnimalIslandTypography` 及 `MaterialTheme` 获取；尊重夜间模式和背景色设置。
- 项目合集首页与游戏合集首页是不同层级。`AppNavigation.kt` 定义顶层路由和 `NavHost`；游戏内路由放各自的 `*Navigation.kt`，避免继续把各游戏页面堆进 `App.kt` 或用条件分支代替导航。
- 游戏合集的首页与统计使用 `NavigationSuiteScaffold` 自适应导航，内容由内部 `NavHost` 承载；二级页面隐藏导航。返回按钮和系统返回键都应回到正确的上一级页面。
- 布局同时考虑手机与宽屏/Web：按可用宽高约束棋盘或画板尺寸，为操作区留出空间，处理系统状态栏和安全区域。状态切换与翻页等交互优先用 Compose 动画 API，避免突变。

## 游戏状态与数据

- 规则和状态转换尽量写成可测试的共享 Kotlin 逻辑，Composable 负责呈现与事件转发。现有游戏通常用 `remember*Session` 管理当前局、续玩、统计和序列化。
- 持久化通过 `App` 的初始值与 `on...Change` 回调连接各平台；Android 入口目前用 `SharedPreferences`。修改统计或存档格式时同时检查恢复、完成条件、续玩及对应测试，不要假定所有平台使用同一种存储。
- 好友对战的通信实现位于 Android 平台，公共对战协议与游戏状态位于共享代码。修改对战时注意断线、重连/退出、实时同步以及将来跨平台传输的边界。

## 构建与部署

- 依赖版本集中在 `gradle/libs.versions.toml`。确认当前插件和目标配置后再新增依赖，不把 Android 专用依赖放入 `commonMain`。
- Web 同时有 JS 与 Wasm 入口；Cloudflare Pages workflow 在 `.github/workflows/deploy-pages.yml`，构建任务为 `:webApp:wasmJsBrowserDistribution`。部署目标为 Pages 项目 `muse-island`，生产分支为 `xiaoqing-island`；其他分支走预览。部署说明见 `docs/cloudflare-pages.md`。不要在仓库中写入 Cloudflare 凭据。
