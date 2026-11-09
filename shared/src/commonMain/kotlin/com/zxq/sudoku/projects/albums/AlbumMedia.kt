package com.zxq.sudoku.projects.albums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

internal data class AlbumMedia(
    val uri: String,
    val preview: ImageBitmap?,
    val isVideo: Boolean,
    val capturedAt: Long,
    val aspectRatio: Float,
)

@Composable
internal expect fun rememberAlbumMediaPicker(
    onLoadingChange: (Boolean) -> Unit,
    onPicked: (List<AlbumMedia>) -> Unit,
): () -> Unit

internal expect fun openAlbumVideo(uri: String)

internal expect val albumMediaImportSupported: Boolean
