package com.nuvio.tv.core.player

/** Once enabled, retain AC-3 for this media session even at zero gain. */
object CenterChannelGainPolicy {
    fun needsRouteRebuild(gainDb: Int, routeBuiltForGain: Boolean, eligible: Boolean): Boolean =
        eligible && gainDb.coerceIn(0, 6) > 0 && !routeBuiltForGain

    fun forceAc3(gainDb: Int, retainedForSession: Boolean, eligible: Boolean): Boolean =
        eligible && (gainDb.coerceIn(0, 6) > 0 || retainedForSession)
}
