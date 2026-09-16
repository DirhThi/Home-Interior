package com.interiordesign3d.ui.screen.language.normal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.interiordesign3d.ads.TrackingEvent
import com.interiordesign3d.ui.properties.GeneralBackHandler
import com.interiordesign3d.ui.properties.TrackingScreen
import com.interiordesign3d.ui.screen.language.LanguageViewModel
import com.interiordesign3d.ui.screen.language.view.LanguageContent

@Composable
fun LanguageScreen(viewModel: LanguageViewModel) {
    TrackingScreen(screen = "ScrLanguage")
    LaunchedEffect(Unit) { viewModel.preloadAltAd() }

    LanguageContent(state = viewModel.screenState, showBack = false, adPlacement = "language")
    GeneralBackHandler(showInterAd = false) { TrackingEvent.logEvent("ScrLanguage_back") }
}
