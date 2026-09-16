package com.interiordesign3d.ui.screen.language

import android.app.Application
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.interiordesign3d.common.base.BaseViewModel
import com.interiordesign3d.data.repository.AppPrefs
import com.interiordesign3d.data.repository.LanguageManager
import com.interiordesign3d.ui.navigation.Dest
import com.interiordesign3d.ui.screen.language.state.LanguageState

class LanguageViewModel(
    app: Application,
    backStack: NavBackStack<NavKey>,
    private val fromSettings: Boolean,
) : BaseViewModel(app, backStack) {

    val screenState: LanguageState = object : LanguageState() {
        override fun onConfirm() {
            LanguageManager.apply(picked)
            if (fromSettings) pops() else goOn()
        }

        override fun onBack() {
            if (fromSettings) pops()
        }
    }

    init {
        LanguageManager.sync()
        screenState.picked = LanguageManager.current
        screenState.firstOpen = !fromSettings
    }

    private fun goOn() {
        val next = if (AppPrefs.onboarded) Dest.ScrMain() else Dest.ScrOnboard
        navigateTo(next, popupTos = listOf(Dest.ScrLanguage::class.java))
    }
}
