package com.interiordesign3d.ui.properties

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.interiordesign3d.ads.TrackingEvent

/**
 * Intercepts the system back gesture on a screen the funnel doesn't want left by accident.
 * Always logs, and would show an interstitial first when [showInterAd] is true — that call is a
 * no-op until a real ad SDK is wired in, so [onBack] runs immediately either way.
 */
@Composable
fun GeneralBackHandler(showInterAd: Boolean = true, onBack: () -> Unit) {
    BackHandler {
        TrackingEvent.logEvent("button_click_back")
        onBack()
    }
}

/**
 * Where a native ad slot goes. Renders nothing today — swap the body in for a real ad view once
 * one exists, without the caller (or [nameSpace]) changing.
 */
@Composable
fun AdSlot(nameSpace: String, modifier: Modifier = Modifier) {
    Box(modifier)
}

/** Fires once when the screen it's placed in first shows. A no-op call site for later analytics. */
@Composable
fun TrackingScreen(screen: String) {
    DisposableEffect(screen) {
        TrackingEvent.logScreenShow(screen)
        onDispose {}
    }
}
