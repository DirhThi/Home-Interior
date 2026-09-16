package com.interiordesign3d.ui.screen.select.alternative

import androidx.compose.runtime.Composable
import com.interiordesign3d.ads.TrackingEvent
import com.interiordesign3d.ui.properties.GeneralBackHandler
import com.interiordesign3d.ui.properties.TrackingScreen
import com.interiordesign3d.ui.screen.select.SelectViewModel
import com.interiordesign3d.ui.screen.select.view.SelectContent

/**
 * Same content as [com.interiordesign3d.ui.screen.select.normal.SelectScreen] — this class exists
 * so the step has a second screen identity to track and to hang a second ad slot from.
 */
@Composable
fun SelectAltScreen(viewModel: SelectViewModel) {
    TrackingScreen(screen = "ScrSelectAlt")
    SelectContent(state = viewModel.screenState)
    GeneralBackHandler(showInterAd = false) { TrackingEvent.logEvent("ScrSelectAlt_back") }
}
