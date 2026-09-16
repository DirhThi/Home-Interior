package com.interiordesign3d.ui.screen.language.alternative

import androidx.compose.runtime.Composable
import com.interiordesign3d.ads.TrackingEvent
import com.interiordesign3d.ui.properties.GeneralBackHandler
import com.interiordesign3d.ui.properties.TrackingScreen
import com.interiordesign3d.ui.screen.language.LanguageViewModel
import com.interiordesign3d.ui.screen.language.view.LanguageContent

/**
 * Same content as [com.interiordesign3d.ui.screen.language.normal.LanguageScreen] — this class
 * exists so the step has a second screen identity to track and to hang a second ad slot from.
 */
@Composable
fun LanguageAltScreen(viewModel: LanguageViewModel) {
    TrackingScreen(screen = "ScrLanguageAlt")

    LanguageContent(state = viewModel.screenState, showBack = false, adPlacement = "language_alt")

    ConfirmLanguageDialog(
        isShown = viewModel.screenState.showConfirmLanguage,
        onDismiss = viewModel.screenState::onConfirmLanguageDismiss,
        onConfirm = viewModel.screenState::onConfirmLanguageAccept,
    )

    GeneralBackHandler(showInterAd = false) { TrackingEvent.logEvent("ScrLanguageAlt_back") }
}
