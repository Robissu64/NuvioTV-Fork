package com.nuvio.tv.ui.screens.player

import android.util.Log
import com.nuvio.tv.core.player.CenterChannelGainPolicy
import com.nuvio.tv.data.local.PlayerSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun PlayerRuntimeController.isCenterGainRouteEligible(settings: PlayerSettings): Boolean =
    !isUsingMpvEngine() && settings.decoderPriority != 0 &&
        currentAudioOutputRoute?.isBluetooth != true &&
        !AudioOutputRouteDetector.isBluetoothMediaOutput(context)

internal fun PlayerRuntimeController.applyCenterChannelGainSettings(settings: PlayerSettings) {
    val eligible = isCenterGainRouteEligible(settings)
    val retained = centerGainAc3StreamUrl == currentStreamUrl
    val forceAc3 = CenterChannelGainPolicy.forceAc3(settings.centerChannelGainDb, retained, eligible)
    _uiState.update { it.copy(
        centerChannelGainDb = settings.centerChannelGainDb,
        isCenterChannelGainAvailable = eligible
    ) }
    ffmpegAudioRenderer?.setCenterChannelGainDb(settings.centerChannelGainDb)
    ffmpegAudioRenderer?.setForceOpticalPassthrough(forceAc3)
    val routeBuilt = lastExoConstructionFingerprint?.forceOpticalPassthroughActive == true
    if (_exoPlayer == null || centerGainRebuildInFlight ||
        !CenterChannelGainPolicy.needsRouteRebuild(settings.centerChannelGainDb, routeBuilt, eligible)) return
    centerGainRebuildJob?.cancel()
    val stream = currentStreamUrl
    centerGainRebuildJob = scope.launch {
        // Coalesce remote-control clicks; re-read the persisted value before rebuilding.
        delay(150)
        if (currentStreamUrl != stream || _exoPlayer == null || centerGainRebuildInFlight) return@launch
        val latest = currentPlayerSettingsForReport
        if (!isCenterGainRouteEligible(latest) || latest.centerChannelGainDb <= 0) return@launch
        val player = _exoPlayer ?: return@launch
        val position = player.currentPosition.coerceAtLeast(0L)
        val paused = !player.playWhenReady || userPausedManually
        rememberCurrentTrackPreferenceForEngineSwitch()
        player.playWhenReady = false
        centerGainAc3StreamUrl = stream
        centerGainRebuildInFlight = true
        pendingResumeProgress = null
        _uiState.update { it.copy(pendingSeekPosition = position) }
        Log.i(PlayerRuntimeController.TAG,
            "CENTER_GAIN_ROUTE: rebuild AC3 gain=${latest.centerChannelGainDb} position=$position paused=$paused")
        initializePlayer(stream, currentHeaders, startPaused = paused, preserveCenterGainPlayback = true)
    }
}
