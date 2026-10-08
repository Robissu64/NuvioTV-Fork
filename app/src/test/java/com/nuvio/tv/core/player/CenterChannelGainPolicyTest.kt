package com.nuvio.tv.core.player

import org.junit.Assert.*
import org.junit.Test

class CenterChannelGainPolicyTest {
    @Test fun zeroToFourBuildsAc3Once() {
        assertTrue(CenterChannelGainPolicy.needsRouteRebuild(4, false, true))
        assertTrue(CenterChannelGainPolicy.forceAc3(4, false, true))
        assertFalse(CenterChannelGainPolicy.needsRouteRebuild(4, true, true))
    }
    @Test fun fourToSixUsesLiveDecoder() {
        assertFalse(CenterChannelGainPolicy.needsRouteRebuild(6, true, true))
        assertTrue(CenterChannelGainPolicy.forceAc3(6, true, true))
    }
    @Test fun sixToZeroKeepsAc3WithoutBoostOrRebuild() {
        assertFalse(CenterChannelGainPolicy.needsRouteRebuild(0, true, true))
        assertTrue(CenterChannelGainPolicy.forceAc3(0, true, true))
        assertFalse(CenterChannelGainPolicy.forceAc3(0, false, true))
    }
    @Test fun zeroToSixBuildsAc3Once() {
        assertTrue(CenterChannelGainPolicy.needsRouteRebuild(6, false, true))
        assertTrue(CenterChannelGainPolicy.forceAc3(6, false, true))
    }
    @Test fun bluetoothAndDeviceOnlyNeverForceAc3() {
        assertFalse(CenterChannelGainPolicy.needsRouteRebuild(6, false, false))
        assertFalse(CenterChannelGainPolicy.forceAc3(6, true, false))
    }
    @Test fun negativeAndZeroNeverActivateRoute() {
        assertFalse(CenterChannelGainPolicy.needsRouteRebuild(-1, false, true))
        assertFalse(CenterChannelGainPolicy.forceAc3(0, false, true))
    }
}
