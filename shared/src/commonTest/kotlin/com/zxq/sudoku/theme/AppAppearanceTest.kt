package com.zxq.sudoku.theme

import androidx.compose.runtime.mutableStateOf
import com.zxq.sudoku.games.sudoku.GamePalette
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppAppearanceTest {
    @Test
    fun themeModeFollowsSelectionAndSystem() {
        assertFalse(resolveDarkTheme(ThemeMode.FollowSystem, GamePalette.Island, false))
        assertTrue(resolveDarkTheme(ThemeMode.FollowSystem, GamePalette.Island, true))
        assertFalse(resolveDarkTheme(ThemeMode.Light, GamePalette.Island, true))
        assertTrue(resolveDarkTheme(ThemeMode.Dark, GamePalette.Island, false))
        assertTrue(resolveDarkTheme(ThemeMode.Light, GamePalette.Black, false))
    }

    @Test
    fun appearanceChangesNotifyPersistenceOnlyOnce() {
        val modes = mutableListOf<String>()
        val palettes = mutableListOf<String>()
        val appearance = AppAppearance(
            mutableStateOf(ThemeMode.FollowSystem), mutableStateOf(GamePalette.Island),
            modes::add, palettes::add,
        )

        appearance.mode = ThemeMode.Dark
        appearance.mode = ThemeMode.Dark
        appearance.palette = GamePalette.Blue
        appearance.palette = GamePalette.Blue

        assertEquals(listOf("Dark"), modes)
        assertEquals(listOf("Blue"), palettes)
    }
}
