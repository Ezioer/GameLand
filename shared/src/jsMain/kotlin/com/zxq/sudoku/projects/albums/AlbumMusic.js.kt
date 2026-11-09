package com.zxq.sudoku.projects.albums

import androidx.compose.runtime.Composable

internal actual val albumMusicSupported: Boolean = false

@Composable
internal actual fun rememberAlbumMusicPicker(onPicked: (String) -> Unit): () -> Unit = {}

@Composable
internal actual fun AlbumBackgroundMusic(uri: String?) = Unit
