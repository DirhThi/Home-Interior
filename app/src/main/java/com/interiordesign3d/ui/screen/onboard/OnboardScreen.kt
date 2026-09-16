package com.interiordesign3d.ui.screen.onboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.interiordesign3d.ads.TrackingEvent
import com.interiordesign3d.ui.properties.GeneralBackHandler
import com.interiordesign3d.ui.screen.onboard.view.OnboardContent

@Composable
fun OnboardScreen(viewModel: OnboardViewModel) {
    val trackedPages = remember { mutableSetOf<String>() }

    OnboardContent(
        slots = viewModel.slots,
        onPageShown = { name -> if (trackedPages.add(name)) TrackingEvent.logScreenShow(name) },
        onLastPageShown = viewModel::preloadNextScreen,
        onFinished = viewModel::onFinish,
    )

    GeneralBackHandler(showInterAd = false) { TrackingEvent.logEvent("ScrOnboard_back") }
}
