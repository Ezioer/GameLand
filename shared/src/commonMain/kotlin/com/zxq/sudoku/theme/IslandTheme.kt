package com.zxq.sudoku.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val IslandGreen = Color(0xFF3D7567)
internal val Sand = Color(0xFFFFF8E9)
internal val Ink = Color(0xFF24483F)

@Composable
internal fun IslandTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = AnimalIslandTypography(),
        colorScheme = lightColorScheme(
            primary = IslandGreen,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD7EEE2),
            onPrimaryContainer = Ink,
            secondaryContainer = Color(0xFFFFEBC5),
            background = Sand,
            onBackground = Ink,
            surface = Sand,
            onSurface = Ink,
            surfaceContainer = Color(0xFFFFFDF5),
        ),
        content = content,
    )
}
