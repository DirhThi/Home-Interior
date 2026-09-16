package com.interiordesign3d.ui.screen.settings.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.interiordesign3d.common.base.BaseScreenState

@Stable
open class SettingsState : BaseScreenState() {
    var showCredits by mutableStateOf(false)

    open fun onShowCredits() { showCredits = true }
    open fun onLanguage() {}
    open fun onDismissCredits() { showCredits = false }
}
