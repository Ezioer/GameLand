package com.zxq.sudoku.projects.albums

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

internal actual val albumMusicSupported: Boolean = true

@Composable
internal actual fun rememberAlbumMusicPicker(onPicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onPicked(uri.toString())
        }
    }
    return { launcher.launch(arrayOf("audio/*")) }
}

@Composable
internal actual fun AlbumBackgroundMusic(uri: String?) {
    val context = LocalContext.current
    DisposableEffect(context, uri) {
        val player = uri?.let { runCatching { MediaPlayer.create(context, Uri.parse(it)) }.getOrNull() }
        player?.isLooping = true
        runCatching { player?.start() }
        onDispose { runCatching { player?.release() } }
    }
}
