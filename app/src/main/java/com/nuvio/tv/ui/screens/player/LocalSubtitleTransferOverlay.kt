package com.nuvio.tv.ui.screens.player

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.nuvio.tv.R
import com.nuvio.tv.core.qr.QrCodeGenerator
import com.nuvio.tv.core.server.DeviceIpAddress
import com.nuvio.tv.core.server.LocalSubtitleTransferServer
import com.nuvio.tv.ui.screens.addon.QrCodeOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal fun LocalSubtitleTransferOverlay(onEvent: (PlayerEvent) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val active = remember { AtomicBoolean(true) }
    var serverUrl by remember { mutableStateOf<String?>(null) }
    var qr by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var instruction by remember { mutableStateOf(context.getString(R.string.subtitle_local_transfer_loading)) }
    val server = remember {
        LocalSubtitleTransferServer(
            onSubtitle = { name, bytes ->
                Handler(Looper.getMainLooper()).post {
                    if (active.get()) {
                        onEvent(PlayerEvent.OnUploadLocalSubtitle(name, bytes))
                        onClose()
                    }
                }
            },
            pageTitle = context.getString(R.string.subtitle_local_transfer),
            chooseLabel = context.getString(R.string.subtitle_local_send),
            successLabel = context.getString(R.string.subtitle_local_received),
            errorLabel = context.getString(R.string.subtitle_local_read_error)
        )
    }
    DisposableEffect(server) {
        onDispose { active.set(false); server.stop() }
    }
    LaunchedEffect(server) {
        try {
            val result = withContext(Dispatchers.IO) {
                val ip = DeviceIpAddress.get(context) ?: error("No LAN address")
                server.start(15_000, true)
                val url = "http://$ip:${server.listeningPort}/${server.token}"
                url to QrCodeGenerator.generate(url, 420)
            }
            serverUrl = result.first
            qr = result.second
            instruction = context.getString(R.string.subtitle_local_transfer_instruction)
            kotlinx.coroutines.delay(10 * 60_000L)
            active.set(false)
            server.stop()
            serverUrl = null
            qr = null
            instruction = context.getString(R.string.subtitle_local_transfer_expired)
        } catch (e: kotlinx.coroutines.CancellationException) {
            server.stop()
            throw e
        } catch (_: Exception) {
            server.stop()
            instruction = context.getString(R.string.subtitle_local_transfer_error)
        }
    }
    QrCodeOverlay(qrBitmap = qr, serverUrl = serverUrl, instruction = instruction, onClose = onClose)
}
