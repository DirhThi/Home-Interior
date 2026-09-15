package com.interiordesign3d.ui.screen.settings

import android.app.Application
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.ui.screen.settings.state.SettingsState

class SettingsViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    val screenState: SettingsState = object : SettingsState() {}
}
