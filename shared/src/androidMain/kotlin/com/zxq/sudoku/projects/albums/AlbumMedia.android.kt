package com.zxq.sudoku.projects.albums

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal actual fun rememberAlbumMediaPicker(
    onLoadingChange: (Boolean) -> Unit,
    onPicked: (List<AlbumMedia>) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    SideEffect { AlbumVideoContext.context = context.applicationContext }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            onLoadingChange(true)
            try {
                val items = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri ->
                        runCatching {
                            val video = context.contentResolver.getType(uri)?.startsWith("video/") == true
                            val bitmap = if (video) {
                                val retriever = MediaMetadataRetriever()
                                try {
                                    retriever.setDataSource(context, uri)
                                    if (Build.VERSION.SDK_INT >= 27)
                                        retriever.getScaledFrameAtTime(-1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 960, 960)
                                    else retriever.getFrameAtTime(-1)
                                } finally {
                                    retriever.release()
                                }
                            } else {
                                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                                val sample = (maxOf(bounds.outWidth, bounds.outHeight) / 1200).coerceAtLeast(1)
                                context.contentResolver.openInputStream(uri)?.use {
                                    BitmapFactory.decodeStream(it, null,
                                        BitmapFactory.Options().apply { inSampleSize = sample })
                                }
                            }
                            AlbumMedia(uri.toString(), bitmap?.asImageBitmap(), video,
                                mediaDate(context.contentResolver, uri),
                                if (bitmap != null && bitmap.height > 0) bitmap.width.toFloat() / bitmap.height else 1f)
                        }.getOrNull()
                    }
                }
                onPicked(items)
            } finally {
                onLoadingChange(false)
            }
        }
    }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
}

private fun mediaDate(resolver: android.content.ContentResolver, uri: Uri): Long {
    return runCatching {
        resolver.query(uri, arrayOf(MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_ADDED), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use 0L
            val taken = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_TAKEN)
            val added = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
            when {
                taken >= 0 && !cursor.isNull(taken) -> cursor.getLong(taken)
                added >= 0 && !cursor.isNull(added) -> cursor.getLong(added) * 1000L
                else -> 0L
            }
        } ?: 0L
    }.getOrDefault(0L)
}

internal actual fun openAlbumVideo(uri: String) {
    val context = AlbumVideoContext.context ?: return
    val parsed = Uri.parse(uri)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(parsed, "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

internal object AlbumVideoContext {
    var context: android.content.Context? = null
}

internal actual val albumMediaImportSupported: Boolean = true
