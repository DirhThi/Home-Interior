package com.interiordesign3d.ui.screen.select.normal

import androidx.compose.runtime.Composable
import com.interiordesign3d.ads.TrackingEvent
import com.interiordesign3d.ui.properties.GeneralBackHandler
import com.interiordesign3d.ui.properties.TrackingScreen
import com.interiordesign3d.ui.screen.select.SelectViewModel
import com.interiordesign3d.ui.screen.select.view.SelectContent

@Composable
fun SelectScreen(viewModel: SelectViewModel) {
    TrackingScreen(screen = "ScrSelect")
    SelectContent(state = viewModel.screenState)
    GeneralBackHandler(showInterAd = false) { TrackingEvent.logEvent("ScrSelect_back") }
}
