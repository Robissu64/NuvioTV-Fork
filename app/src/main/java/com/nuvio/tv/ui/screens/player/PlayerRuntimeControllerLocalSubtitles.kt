package com.nuvio.tv.ui.screens.player

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.nuvio.tv.R
import com.nuvio.tv.core.player.LocalSubtitleFiles
import com.nuvio.tv.domain.model.Subtitle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.coroutines.coroutineContext

internal fun PlayerRuntimeController.importLocalSubtitle(
    uri: Uri? = null, suppliedName: String? = null, suppliedBytes: ByteArray? = null
) {
    cancelLocalSubtitleImport()
    val generation = localSubtitleImportGeneration
    val streamAtSelection = currentStreamUrl
    val videoAtSelection = currentVideoId
    localSubtitleImportJob = scope.launch {
        _uiState.update { it.copy(isImportingLocalSubtitle = true, localSubtitleError = null) }
        var cachedFile: File? = null
        try {
            val imported = withContext(Dispatchers.IO) {
                val name = suppliedName ?: if (uri?.scheme == "content") {
                    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                } else uri?.lastPathSegment
                val extension = LocalSubtitleFiles.extension(name.orEmpty())
                    ?: error(context.getString(R.string.subtitle_local_format_error))
                require(suppliedBytes != null || uri?.scheme in setOf("content", "file"))
                val bytes = suppliedBytes ?: context.contentResolver.openInputStream(requireNotNull(uri))?.use { input ->
                    val jobContext = coroutineContext
                    LocalSubtitleFiles.readBounded(input) { jobContext.ensureActive() }
                } ?: error(context.getString(R.string.subtitle_local_read_error))
                val decoded = LocalSubtitleFiles.decodeAndValidate(bytes, extension)
                coroutineContext.ensureActive()
                // Never derive a cache path from an untrusted provider filename.
                val file = File(localSubtitleCacheDir, "${UUID.randomUUID()}.$extension")
                cachedFile = file
                file.writeText(decoded, Charsets.UTF_8)
                val activeName = Uri.parse(_uiState.value.selectedAddonSubtitle?.url.orEmpty()).lastPathSegment
                localSubtitleCacheDir.listFiles()?.filter { it.name != file.name && it.name != activeName }
                    ?.sortedByDescending { it.lastModified() }?.drop(2)?.forEach { it.delete() }
                Subtitle(
                    id = "local:${file.name}", url = Uri.fromFile(file).toString(),
                    lang = "und", addonName = context.getString(R.string.subtitle_local_source, name.orEmpty().take(160)),
                    addonLogo = null, isLocal = true
                )
            }
            if (generation != localSubtitleImportGeneration ||
                currentStreamUrl != streamAtSelection || currentVideoId != videoAtSelection) {
                cachedFile?.delete()
                return@launch
            }
            // One local choice per film keeps the cache bounded; keep old files until exit
            // because a renderer may still be reading its previous selection.
            localSubtitles = listOf(imported)
            localSubtitleMediaKey = "$streamAtSelection|$videoAtSelection"
            _uiState.update { it.copy(
                addonSubtitles = (it.addonSubtitles.filterNot { s -> s.isLocal } + imported),
                isImportingLocalSubtitle = false
            ) }
            // The selection event cancels pending imports. This import is already complete.
            localSubtitleImportJob = null
            onEvent(PlayerEvent.OnSelectAddonSubtitle(imported))
        } catch (e: CancellationException) {
            cachedFile?.delete()
            throw e
        } catch (e: Exception) {
            cachedFile?.delete()
            val message = context.getString(R.string.subtitle_local_read_error)
            _uiState.update { it.copy(localSubtitleError = message) }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } finally {
            if (generation == localSubtitleImportGeneration) {
                _uiState.update { it.copy(isImportingLocalSubtitle = false) }
            }
        }
    }
}

internal fun PlayerRuntimeController.clearLocalSubtitleSelection() {
    cancelLocalSubtitleImport()
    localSubtitles = emptyList()
    localSubtitleMediaKey = null
    _uiState.update { it.copy(localSubtitleError = null) }
}

internal fun PlayerRuntimeController.cancelLocalSubtitleImport() {
    localSubtitleImportGeneration++
    localSubtitleImportJob?.cancel()
    localSubtitleImportJob = null
    _uiState.update { it.copy(isImportingLocalSubtitle = false, localSubtitleError = null) }
}

internal fun PlayerRuntimeController.clearLocalSubtitlesForNewMedia() {
    if (localSubtitleMediaKey != "$currentStreamUrl|$currentVideoId") clearLocalSubtitleSelection()
}
