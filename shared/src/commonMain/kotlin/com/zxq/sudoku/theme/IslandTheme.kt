package com.zxq.sudoku.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.zxq.sudoku.games.sudoku.GamePalette

internal val IslandGreen = Color(0xFF3D7567)
internal val Sand = Color(0xFFFFF8E9)
internal val Ink = Color(0xFF24483F)

@Composable
internal fun IslandTheme(dark: Boolean, palette: GamePalette, content: @Composable () -> Unit) {
    val background = if (dark) {
        if (palette.isDark) palette.background else lerp(Color(0xFF141B1A), palette.swatch, 0.1f)
    } else palette.background
    val accent = if (dark) lerp(Color.White, palette.swatch, 0.55f)
        else if (palette == GamePalette.White) Ink else palette.accent
    val scheme = if (dark) darkColorScheme(
        primary = accent,
        onPrimary = Color(0xFF17231F),
        primaryContainer = lerp(background, palette.swatch, 0.23f),
        onPrimaryContainer = Color(0xFFF5F4EC),
        secondaryContainer = Color(0xFF38362F),
        onSecondaryContainer = Color(0xFFF5F4EC),
        background = background,
        onBackground = Color(0xFFF5F4EC),
        surface = background,
        onSurface = Color(0xFFF5F4EC),
        surfaceContainer = Color(0xFF292E2C),
    ) else lightColorScheme(
        primary = accent,
        onPrimary = Color.White,
        primaryContainer = lerp(Color.White, palette.swatch, 0.18f),
        onPrimaryContainer = Ink,
        secondaryContainer = Color(0xFFFFEBC5),
        background = background,
        onBackground = palette.ink,
        surface = background,
        onSurface = palette.ink,
        surfaceContainer = Color(0xFFFFFDF5),
    )
    MaterialTheme(
        typography = AnimalIslandTypography(),
        colorScheme = scheme,
        content = content,
    )
}
