package com.interiordesign3d.ui.screen.language.setting

import android.app.Application
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.repository.LanguageManager
import com.interiordesign3d.ui.screen.language.state.LanguageState

/** Reached from Settings only — one screen, a back button, no Alt step and no ad slot. */
class LanguageSettingViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
) : BaseViewModel(app, backStack) {

    val screenState: LanguageState = object : LanguageState() {
        override fun onConfirm() {
            LanguageManager.apply(picked)
            pops()
        }

        override fun onBack() = pops()
    }

    init {
        LanguageManager.sync()
        screenState.picked = LanguageManager.current
    }
}
