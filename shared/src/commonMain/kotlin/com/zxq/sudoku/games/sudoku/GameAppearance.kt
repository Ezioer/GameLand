package com.zxq.sudoku.games.sudoku

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// AnimalIslandUI/theme/Color.kt: PrimaryColor, BgColorContent, TextColor and App palette.
enum class GamePalette(val label: String, val swatch: Color) {
    Island("小岛青", Color(0xFF19C8B9)),
    Pink("樱花粉", Color(0xFFF8A6B2)),
    Blue("海风蓝", Color(0xFF889DF0)),
    Yellow("暖阳黄", Color(0xFFF7CD67)),
    Green("青草绿", Color(0xFF8AC68A)),
    White("纯白", Color.White),
    Black("深黑", Color(0xFF202020));

    val isDark: Boolean get() = this == Black
    val background: Color get() = when (this) {
        Black -> Color(0xFF151515)
        White -> Color.White
        Island -> Color(0xFFF7F3DF)
        else -> lerp(Color.White, swatch, 0.12f)
    }
    val board: Color get() = if (isDark) Color(0xFF262626) else Color.White
    val ink: Color get() = when (this) {
        Black -> Color(0xFFF5F5F5)
        White -> Color(0xFF202020)
        else -> Color(0xFF794F27)
    }
    val accent: Color get() = when (this) {
        Black -> Color(0xFFE1E1E1)
        White -> Color(0xFF202020)
        else -> lerp(ink, swatch, 0.55f)
    }
    val selected: Color get() = when (this) {
        Black -> Color(0xFF505050)
        White -> Color(0xFFDCDCDC)
        Island -> Color(0xFFFFCC00)
        else -> lerp(Color.White, swatch, 0.6f)
    }
    val related: Color get() = if (isDark) Color(0xFF333333) else lerp(Color.White, swatch, 0.18f)
    val sameDigit: Color get() = if (isDark) Color(0xFF454545) else lerp(Color.White, swatch, 0.36f)

    val colorScheme: ColorScheme get() =
        if (isDark) {
            darkColorScheme(
                primary = accent,
                onPrimary = Color.Black,
                background = background,
                onBackground = ink,
                surface = background,
                onSurface = ink,
                surfaceContainer = board,
            )
        } else {
            lightColorScheme(
                primary = accent,
                onPrimary = Color.White,
                background = background,
                onBackground = ink,
                surface = background,
                onSurface = ink,
                surfaceContainer = board,
            )
        }
}

enum class GameFontSize(val label: String, val cell: TextUnit, val note: TextUnit, val keypad: TextUnit) {
    Small("小", 18.sp, 7.sp, 21.sp),
    Medium("中", 21.sp, 8.sp, 25.sp),
    Large("大", 25.sp, 10.sp, 29.sp),
}
