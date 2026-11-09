package com.zxq.sudoku.projects.albums

import androidx.compose.runtime.Composable

internal expect val albumMusicSupported: Boolean

@Composable
internal expect fun rememberAlbumMusicPicker(onPicked: (String) -> Unit): () -> Unit

@Composable
internal expect fun AlbumBackgroundMusic(uri: String?)
