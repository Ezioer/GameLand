package com.zxq.sudoku.projects.albums

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberAlbumMediaPicker(onLoadingChange: (Boolean) -> Unit,
    onPicked: (List<AlbumMedia>) -> Unit): () -> Unit = {}

internal actual fun openAlbumVideo(uri: String) = Unit

internal actual val albumMediaImportSupported: Boolean = false
