package com.zxq.sudoku.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.zxq.sudoku.games.sudoku.GamePalette

internal enum class ThemeMode(val label: String) {
    FollowSystem("跟随系统"), Light("白天"), Dark("夜间"),
}

internal class AppAppearance(
    private val modeState: MutableState<ThemeMode>,
    val paletteState: MutableState<GamePalette>,
    private val onModeChange: (String) -> Unit,
    private val onPaletteChange: (String) -> Unit,
) {
    var mode: ThemeMode
        get() = modeState.value
        set(value) {
            if (modeState.value != value) {
                modeState.value = value
                onModeChange(value.name)
            }
        }

    var palette: GamePalette
        get() = paletteState.value
        set(value) {
            if (paletteState.value != value) {
                paletteState.value = value
                onPaletteChange(value.name)
            }
        }
}

@Composable
internal fun rememberAppAppearance(
    initialMode: String,
    onModeChange: (String) -> Unit,
    initialPalette: String,
    onPaletteChange: (String) -> Unit,
): AppAppearance {
    val mode = rememberSaveable {
        mutableStateOf(ThemeMode.entries.firstOrNull { it.name == initialMode } ?: ThemeMode.FollowSystem)
    }
    val palette = rememberSaveable {
        mutableStateOf(GamePalette.entries.firstOrNull { it.name == initialPalette } ?: GamePalette.Island)
    }
    return remember(mode, palette, onModeChange, onPaletteChange) {
        AppAppearance(mode, palette, onModeChange, onPaletteChange)
    }
}

@Composable
internal fun darkThemeEnabled(mode: ThemeMode, palette: GamePalette): Boolean {
    return resolveDarkTheme(mode, palette, isSystemInDarkTheme())
}

internal fun resolveDarkTheme(mode: ThemeMode, palette: GamePalette, systemDark: Boolean): Boolean {
    return palette.isDark || when (mode) {
        ThemeMode.FollowSystem -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
}
