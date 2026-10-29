import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.zxq.sudoku.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "游戏小岛"
            packageVersion = "1.0.0"

            // 可选：配置快捷方式和图标
            windows {
                iconFile.set(project.file("src/main/resources/assets/game.ico")) // 必须是 .ico 格式
                menu = true // 是否在开始菜单创建快捷方式
                shortcut = true // 是否在桌面创建快捷方式
            }
        }
    }
}